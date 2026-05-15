# pg-basebackup.ps1
# Snapshot fisico completo del cluster (todas las BDs en un solo paso).
# Sirve como base para PITR junto con el archivo de WAL.
#
# Uso:
#   powershell -ExecutionPolicy Bypass -File pg-basebackup.ps1
#
# Requisitos:
#   - El usuario debe tener atributo REPLICATION (postgres lo tiene; o
#     usa backup_user creado con WITH REPLICATION).
#   - pg_hba.conf debe permitir replicacion local. Por defecto suele estar:
#       host  replication  all  127.0.0.1/32  scram-sha-256
#     Si no lo permite, agregar la linea y recargar (pg_ctl reload).
#   - max_wal_senders >= 2 en postgresql.conf.

$ErrorActionPreference = "Stop"

# === CONFIG ===
$PgBin    = "C:\Program Files\PostgreSQL\18\bin"
$PgHost   = "localhost"
$PgPort   = 5432
$PgUser   = "postgres"
$BackupRoot = "C:\catjard-backups"
$BaseDir  = Join-Path $BackupRoot "basebackup"
$LogDir   = Join-Path $BackupRoot "logs"

# === SETUP ===
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$target    = Join-Path $BaseDir "base_$timestamp"
$logFile   = Join-Path $LogDir "pg-basebackup_$timestamp.log"

if (!(Test-Path $BaseDir)) { New-Item -ItemType Directory -Path $BaseDir -Force | Out-Null }
if (!(Test-Path $LogDir))  { New-Item -ItemType Directory -Path $LogDir  -Force | Out-Null }

function Write-Log {
    param([string]$msg)
    $line = "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] $msg"
    Write-Output $line
    Add-Content -Path $logFile -Value $line
}

Write-Log "=== Inicio pg_basebackup -> $target ==="

$pgBaseBackup = Join-Path $PgBin "pg_basebackup.exe"
if (!(Test-Path $pgBaseBackup)) {
    Write-Log "ERROR: no se encontro pg_basebackup en $pgBaseBackup"
    exit 1
}

# -X stream  : copia los WAL generados durante el backup (consistencia)
# -P         : reporta progreso
# -R         : escribe standby.signal y postgresql.auto.conf (util si luego
#              quieres montarlo como replica; inocuo si lo usas para restore)
# -F p       : formato 'plain' (directorio igual al PGDATA original)
& $pgBaseBackup `
    --host=$PgHost `
    --port=$PgPort `
    --username=$PgUser `
    --pgdata=$target `
    --format=plain `
    --wal-method=stream `
    --progress `
    --write-recovery-conf `
    --label="catjard_$timestamp"

if ($LASTEXITCODE -ne 0) {
    Write-Log "FAIL exitcode=$LASTEXITCODE"
    exit 1
}

$sizeMB = [math]::Round(((Get-ChildItem $target -Recurse | Measure-Object Length -Sum).Sum) / 1MB, 2)
Write-Log "OK  base creada (${sizeMB} MB)"
Write-Log "=== Fin pg_basebackup ==="
