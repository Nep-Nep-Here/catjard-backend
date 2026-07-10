#!/usr/bin/env bash
# Instala el cron del respaldo diario en el Droplet (idempotente: reemplaza la
# entrada anterior si ya existia). El Droplet esta en UTC: 07:00 UTC = 02:00 Peru.
set -euo pipefail

SCRIPT="$(cd "$(dirname "$0")" && pwd)/backup-diario.sh"
chmod +x "$SCRIPT"

( crontab -l 2>/dev/null | grep -v 'backup-diario.sh' ; \
  echo "0 7 * * * /bin/bash $SCRIPT   # Cat Jard: dump diario 02:00 Peru" ) | crontab -

echo "Cron instalado:"
crontab -l | grep backup-diario.sh
echo
echo "Prueba manual ahora:  bash $SCRIPT && tail -20 ~/catjard-backups/logs/backup-diario.log"
