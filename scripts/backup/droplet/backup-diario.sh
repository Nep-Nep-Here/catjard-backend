#!/usr/bin/env bash
# ============================================================================
# Respaldo diario de las BDs de Cat Jard (cron del Droplet) — Fase 5 del plan
# de Gestion de Continuidad: copia 1 de la regla 3-2-1 (disco del Droplet).
#
# 1) pg_dump (formato custom) de las 7 BDs desde el contenedor de Postgres.
# 2) Depura respaldos con mas de RETENCION_DIAS dias.
# 3) Registra la ejecucion en el modulo de Continuidad (trazabilidad):
#    POST /api/continuidad/respaldos/script con el header X-Backup-Token.
#
# Uso manual:   bash backup-diario.sh
# Cron (2am Peru = 7am UTC): ver install-cron.sh
# ============================================================================
set -uo pipefail

# --- Config (sobreescribible por variables de entorno) ---
BACKEND_DIR="${BACKEND_DIR:-$HOME/catjard/backend}"       # repo con el .env
BACKUP_DIR="${BACKUP_DIR:-$HOME/catjard-backups/dumps}"
LOG_DIR="${LOG_DIR:-$HOME/catjard-backups/logs}"
RETENCION_DIAS="${RETENCION_DIAS:-30}"
POSTGRES_CONTAINER="${POSTGRES_CONTAINER:-catjard-postgres-1}"
API_URL="${API_URL:-http://localhost/api/continuidad/respaldos/script}"
SERVICIO_BD_ID="${SERVICIO_BD_ID:-1}"                     # SRV-001 Base de datos PostgreSQL

BDS=(catjard_identity catjard_catalog catjard_crm catjard_sales
     catjard_inventory catjard_operations catjard_solicitudes)

mkdir -p "$BACKUP_DIR" "$LOG_DIR"
exec >> "$LOG_DIR/backup-diario.log" 2>&1
echo "=== $(date '+%F %T') backup-diario ==="

# Lee una variable del .env del backend (sin 'source': el .env puede tener
# caracteres que rompan al shell).
env_get() { grep -E "^$1=" "$BACKEND_DIR/.env" 2>/dev/null | tail -1 | cut -d= -f2-; }
PGUSER="$(env_get POSTGRES_USER)"; PGUSER="${PGUSER:-postgres}"
TOKEN="$(env_get CONTINUIDAD_BACKUP_TOKEN)"

FECHA="$(date +%Y%m%d_%H%M%S)"
DEST="$BACKUP_DIR/$FECHA"
mkdir -p "$DEST"
INICIO=$(date +%s)

ESTADO="exitoso"
DETALLE="Dump diario de ${#BDS[@]} BDs (cron 02:00 Peru). Retencion $RETENCION_DIAS dias."
OK=0

for bd in "${BDS[@]}"; do
  if docker exec "$POSTGRES_CONTAINER" pg_dump -U "$PGUSER" -Fc "$bd" > "$DEST/$bd.dump"; then
    OK=$((OK + 1))
    echo "  ok  $bd ($(du -h "$DEST/$bd.dump" | cut -f1))"
  else
    ESTADO="fallido"
    DETALLE="Fallo pg_dump de $bd (revisa $LOG_DIR/backup-diario.log)."
    echo "  FALLO $bd"
  fi
done

DURACION=$(( $(date +%s) - INICIO ))
TAMANO_MB=$(du -sm "$DEST" | cut -f1)
echo "Resultado: $ESTADO ($OK/${#BDS[@]} BDs, ${TAMANO_MB} MB, ${DURACION}s)"

# --- Retencion: borra carpetas de dumps con mas de N dias ---
find "$BACKUP_DIR" -mindepth 1 -maxdepth 1 -type d -mtime +"$RETENCION_DIAS" -exec rm -rf {} + 2>/dev/null

# --- Trazabilidad: registra la ejecucion en el panel de Continuidad ---
if [ -n "$TOKEN" ]; then
  HTTP=$(curl -s -o /tmp/backup-registro.json -w "%{http_code}" -X POST "$API_URL" \
    -H "Content-Type: application/json" \
    -H "X-Backup-Token: $TOKEN" \
    -d "{
      \"servicioId\": $SERVICIO_BD_ID,
      \"recurso\": \"${#BDS[@]} BDs PostgreSQL (pg_dump formato custom)\",
      \"tipo\": \"completo\",
      \"destino\": \"droplet_local\",
      \"estado\": \"$ESTADO\",
      \"tamanoMb\": $TAMANO_MB,
      \"duracionSeg\": $DURACION,
      \"mensaje\": \"$DETALLE\"
    }")
  if [ "$HTTP" = "201" ]; then
    echo "Registrado en el panel de Continuidad (HTTP $HTTP)."
  else
    echo "AVISO: no se pudo registrar en el panel (HTTP $HTTP): $(cat /tmp/backup-registro.json 2>/dev/null)"
  fi
else
  echo "AVISO: CONTINUIDAD_BACKUP_TOKEN no esta en $BACKEND_DIR/.env; dump hecho pero sin registrar."
fi

[ "$ESTADO" = "exitoso" ]
