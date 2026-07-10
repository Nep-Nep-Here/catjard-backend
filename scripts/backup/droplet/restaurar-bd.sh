#!/usr/bin/env bash
# ============================================================================
# Restauracion de una BD desde un dump (procedimiento del DRP + demo medible).
# Cronometra la restauracion: ese tiempo es tu RTO REAL medido, el dato que va
# en la tabla de "Resultados esperados" del plan.
#
# Uso:
#   bash restaurar-bd.sh catjard_sales                       # ultimo dump
#   bash restaurar-bd.sh catjard_sales 20260711_070001       # dump especifico
# ============================================================================
set -euo pipefail

BACKUP_DIR="${BACKUP_DIR:-$HOME/catjard-backups/dumps}"
POSTGRES_CONTAINER="${POSTGRES_CONTAINER:-catjard-postgres-1}"
BACKEND_DIR="${BACKEND_DIR:-$HOME/catjard/backend}"

BD="${1:?Uso: restaurar-bd.sh <base_de_datos> [carpeta_dump]}"
CARPETA="${2:-$(ls -1 "$BACKUP_DIR" | sort | tail -1)}"
DUMP="$BACKUP_DIR/$CARPETA/$BD.dump"

[ -f "$DUMP" ] || { echo "No existe el dump: $DUMP"; exit 1; }

env_get() { grep -E "^$1=" "$BACKEND_DIR/.env" 2>/dev/null | tail -1 | cut -d= -f2-; }
PGUSER="$(env_get POSTGRES_USER)"; PGUSER="${PGUSER:-postgres}"

echo "Restaurando $BD desde $DUMP"
echo "ATENCION: se reemplaza el contenido actual de la BD. Ctrl+C para abortar (5 s)..."
sleep 5

INICIO=$(date +%s)

# --clean --if-exists: borra y recrea los objetos antes de restaurar.
docker exec -i "$POSTGRES_CONTAINER" pg_restore -U "$PGUSER" \
    --clean --if-exists --no-owner -d "$BD" < "$DUMP"

DURACION=$(( $(date +%s) - INICIO ))
echo
echo "=== $BD restaurada en ${DURACION} segundos ==="
echo "Ese es el RTO real medido de la BD: anotalo en el documento del plan."
echo "Si el microservicio quedo con cache viejo, reinicialo:"
echo "  cd $BACKEND_DIR && docker compose -f docker-compose.yml -f docker-compose.prod.yml restart <servicio>"
