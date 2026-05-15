# restore-dump.ps1
# Restaura una BD desde un archivo .dump generado por pg-dump-all.ps1.
#
# Uso:
#   .\restore-dump.ps1 -DumpFile "C:\catjard-backups\dumps\catjard_sales_20260508_020000.dump" `
#                      -TargetDb catjard_sales_restore
#
# Por seguridad, NO sobrescribe la BD original a menos que pases -Force.

param(
    [Parameter(Mandatory=$true)]
    [string]$DumpFile,

    [Parameter(Mandatory=$true)]
    [string]$TargetDb,

    [switch]$Force
)

$ErrorActionPreference = "Stop"

$PgBin  = "C:\Program Files\PostgreSQL\18\bin"
$PgHost = "localhost"
$PgPort = 5432
$PgUser = "postgres"

if (!(Test-Path $DumpFile)) {
    Write-Error "Dump file no encontrado: $DumpFile"
    exit 1
}

$psql       = Join-Path $PgBin "psql.exe"
$pgRestore  = Join-Path $PgBin "pg_restore.exe"

# 1. Verificar si la BD existe
$exists = & $psql -h $PgHost -p $PgPort -U $PgUser -tAc "SELECT 1 FROM pg_database WHERE datname='$TargetDb'"

if ($exists -eq "1") {
    if (-not $Force) {
        Write-Error "La BD $TargetDb ya existe. Usa -Force para dropearla y recrear, o elige otro nombre."
        exit 2
    }
    Write-Output "Dropeando BD existente $TargetDb (--Force)"
    & $psql -h $PgHost -p $PgPort -U $PgUser -d postgres -c "DROP DATABASE $TargetDb"
}

# 2. Crear BD vacia
Write-Output "Creando BD $TargetDb"
& $psql -h $PgHost -p $PgPort -U $PgUser -d postgres -c "CREATE DATABASE $TargetDb"

# 3. Restore
Write-Output "Restaurando $DumpFile -> $TargetDb"
& $pgRestore `
    --host=$PgHost `
    --port=$PgPort `
    --username=$PgUser `
    --dbname=$TargetDb `
    --no-owner `
    --no-acl `
    --jobs=4 `
    $DumpFile

if ($LASTEXITCODE -ne 0) {
    Write-Error "pg_restore fallo (exitcode=$LASTEXITCODE)"
    exit $LASTEXITCODE
}

Write-Output "OK  Restore completo en $TargetDb"
