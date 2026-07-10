# Respaldos en el Droplet — Gestión de Continuidad y DRP

Scripts de la **Fase 5** del plan (regla 3-2-1) para el Droplet de producción
(`ubuntu-s-4vcpu-8gb-nyc3`). Complementan a los scripts de `scripts/backup/`
(que son para el Postgres local de Windows en desarrollo).

## La regla 3-2-1 en Cat Jard

| Copia | Medio | Cómo | Frecuencia |
|---|---|---|---|
| 1. Dumps locales | Disco del Droplet | `backup-diario.sh` (cron 02:00 Perú) | Diaria |
| 2. Imagen del Droplet | DigitalOcean Backups | Automático (panel DO, ventana 3–7 a. m. Perú) | Semanal |
| 3. Copia externa | PC del equipo | `descargar-dumps.ps1` (scp) | Semanal / a demanda |

Cada copia queda **registrada en el panel de Continuidad** (`/admin/gerencia/continuidad`
→ pestaña Respaldos): las 1 y 3 vía `POST /api/continuidad/respaldos/script` con el
token compartido; la 2 con el botón «Sincronizar con DigitalOcean» (lee la API de DO).

## Instalación (una sola vez, dentro del Droplet)

```bash
# 0. Tras desplegar el código nuevo: limpiar los CHECK de enum viejos de
#    Hibernate (evita errores "viola la restricción check ..._check").
cd ~/catjard/backend
docker exec -i catjard-postgres-1 psql -U postgres -d catjard_solicitudes \
  < scripts/post-deploy-drop-enum-checks.sql

# 1. Token compartido para que los scripts registren sus ejecuciones
echo "CONTINUIDAD_BACKUP_TOKEN=$(openssl rand -hex 24)" >> .env

# 2. Recrear solicitudes-service con la variable nueva
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d solicitudes-service

# 3. Instalar el cron diario (07:00 UTC = 02:00 Perú)
bash scripts/backup/droplet/install-cron.sh

# 4. Probar ahora mismo
bash scripts/backup/droplet/backup-diario.sh
tail -20 ~/catjard-backups/logs/backup-diario.log
```

Después del paso 4 el respaldo debe aparecer en la pestaña **Respaldos** del panel
con origen «Cron del Droplet».

> Si el contenedor de Postgres no se llama `catjard-postgres-1`, verifica con
> `docker ps` y exporta `POSTGRES_CONTAINER=<nombre>` antes de correr el script.

## Copia externa (desde tu PC con Windows)

```powershell
powershell -ExecutionPolicy Bypass -File scripts\backup\droplet\descargar-dumps.ps1 `
  -ServerIp TU_IP -Token EL_TOKEN_DEL_PASO_1
```

Descarga la carpeta de dumps más reciente a `C:\catjard-backups\droplet\` y la
registra como `copia_externa`.

## Demo del DRP: restaurar midiendo el RTO real

Simulacro para la presentación (usa una BD de negocio, no la de solicitudes,
para no borrar el propio registro de respaldos):

```bash
# 1. "Desastre": borrar datos de una BD (ej. ventas)
docker exec -it catjard-postgres-1 psql -U postgres -d catjard_sales \
  -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"

# 2. Verificar que el portal ya no muestra pedidos (evidencia del incidente)

# 3. Restaurar cronometrando (el script mide el tiempo)
bash scripts/backup/droplet/restaurar-bd.sh catjard_sales

# 4. Verificar recuperación y registrar el incidente como resuelto en el panel
#    → el contador RTO mostrará si se cumplió el objetivo (60 min para la BD)
```

El tiempo que imprime el paso 3 es el **RTO real medido**: ese número va en la
tabla de "Resultados esperados" del documento.
