# pg-dump-all.ps1
# Backup logico diario de las 6 BDs del proyecto catjard.
# Formato custom (-F c) -> permite restore selectivo y compresion.
#
# Uso:
#   powershell -ExecutionPolicy Bypass -File pg-dump-all.ps1
#
# Requisitos:
#   - PostgreSQL 18 instalado en la ruta $PgBin (ajustar si difiere).
#   - Password configurada en %APPDATA%\postgresql\pgpass.conf
#     (host:port:db:user:password) para evitar prompt.

$ErrorActionPreference = "Stop"

# === CONFIG ===
$PgBin    = "C:\Program Files\PostgreSQL\18\bin"
$PgHost   = "localhost"
$PgPort   = 5432
$PgUser   = "postgres"          # cambiar a backup_user si lo creaste
$BackupRoot = "C:\catjard-backups"
$DumpDir  = Join-Path $BackupRoot "dumps"
$LogDir   = Join-Path $BackupRoot "logs"

$Databases = @(
    "catjard_identity",
    "catjard_catalog",
    "catjard_crm",
    "catjard_sales",
    "catjard_inventory",
    "catjard_operations"
)

# === SETUP ===
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$logFile   = Join-Path $LogDir "pg-dump_$timestamp.log"

if (!(Test-Path $DumpDir)) { New-Item -ItemType Directory -Path $DumpDir -Force | Out-Null }
if (!(Test-Path $LogDir))  { New-Item -ItemType Directory -Path $LogDir  -Force | Out-Null }

function Write-Log {
    param([string]$msg)
    $line = "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] $msg"
    Write-Output $line
    Add-Content -Path $logFile -Value $line
}

# === EJECUCION ===
Write-Log "=== Inicio pg_dump (timestamp=$timestamp) ==="

$pgDump = Join-Path $PgBin "pg_dump.exe"
if (!(Test-Path $pgDump)) {
    Write-Log "ERROR: no se encontro pg_dump en $pgDump"
    exit 1
}

$failed = @()
foreach ($db in $Databases) {
    $outFile = Join-Path $DumpDir "${db}_${timestamp}.dump"
    Write-Log "-> Dumping $db -> $outFile"

    & $pgDump `
        --host=$PgHost `
        --port=$PgPort `
        --username=$PgUser `
        --format=custom `
        --compress=6 `
        --no-owner `
        --no-acl `
        --file=$outFile `
        $db

    if ($LASTEXITCODE -ne 0) {
        Write-Log "   FAIL ($db) exitcode=$LASTEXITCODE"
        $failed += $db
    } else {
        $sizeMB = [math]::Round((Get-Item $outFile).Length / 1MB, 2)
        Write-Log "   OK   ($db) ${sizeMB} MB"
    }
}

if ($failed.Count -gt 0) {
    Write-Log "=== TERMINO CON ERRORES en: $($failed -join ', ') ==="
    exit 1
} else {
    Write-Log "=== Todas las BDs respaldadas OK ==="
    exit 0
}
