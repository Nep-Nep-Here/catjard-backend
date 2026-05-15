# configure-wal-archiving.ps1
# -----------------------------------------------------------------------------
# Aplica la configuracion de WAL archiving al postgresql.conf del cluster
# nativo (PostgreSQL 18 en Windows).
#
# Que hace:
#   1) Localiza postgresql.conf.
#   2) Crea un backup .bak con timestamp.
#   3) Asegura que estas 4 directivas queden activas y con el valor correcto:
#        wal_level       = replica
#        archive_mode    = on
#        archive_command = 'copy "%p" "C:\\catjard-backups\\wal_archive\\%f"'
#        max_wal_senders = 10
#      (si la linea esta comentada la descomenta; si tiene otro valor lo
#       reemplaza; si no existe la agrega al final).
#   4) Reinicia el servicio postgresql-x64-18.
#   5) Muestra los SHOW correspondientes para verificar.
#
# Uso:
#   Ejecutar PowerShell COMO ADMINISTRADOR y luego:
#     powershell -ExecutionPolicy Bypass -File configure-wal-archiving.ps1
#
#   Modo dry-run (no escribe nada, solo muestra que haria):
#     powershell -ExecutionPolicy Bypass -File configure-wal-archiving.ps1 -DryRun
#
# Si no se ejecuta como admin o el servicio se llama distinto, los pasos 1-3
# quedan reflejados en disco y el reinicio se puede hacer a mano.
# -----------------------------------------------------------------------------

param(
    [string]$ConfPath    = "C:\Program Files\PostgreSQL\18\data\postgresql.conf",
    [string]$ServiceName = "postgresql-x64-18",
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"

# === Directivas a aplicar ===
# Clave -> linea exacta que debe quedar en el archivo (sin comentario)
$desired = [ordered]@{
    "wal_level"       = "wal_level = replica"
    "archive_mode"    = "archive_mode = on"
    "archive_command" = "archive_command = 'copy ""%p"" ""C:\\catjard-backups\\wal_archive\\%f""'"
    "max_wal_senders" = "max_wal_senders = 10"
}

function Write-Section($title) {
    Write-Host ""
    Write-Host "===== $title =====" -ForegroundColor Cyan
}

# --- 1. Localizar postgresql.conf ---
Write-Section "1. Localizando postgresql.conf"
if (!(Test-Path $ConfPath)) {
    Write-Host "ERROR: no existe $ConfPath" -ForegroundColor Red
    Write-Host "Pasa la ruta correcta con -ConfPath '<ruta>'"
    exit 1
}
Write-Host "OK -> $ConfPath"

# --- 2. Backup del archivo ---
Write-Section "2. Backup de seguridad"
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$backupPath = "$ConfPath.bak_$timestamp"
if ($DryRun) {
    Write-Host "[dry-run] Copiaria $ConfPath -> $backupPath"
} else {
    Copy-Item -Path $ConfPath -Destination $backupPath -Force
    Write-Host "OK -> $backupPath"
}

# --- 3. Aplicar directivas ---
Write-Section "3. Aplicando directivas"
$lines = Get-Content $ConfPath
$newLines = New-Object System.Collections.Generic.List[string]
$applied = @{}
foreach ($k in $desired.Keys) { $applied[$k] = $false }

foreach ($line in $lines) {
    $matched = $false
    foreach ($k in $desired.Keys) {
        # Match: opcionalmente comentada, espacios, key, =, ...
        if ($line -match "^\s*#?\s*${k}\s*=") {
            if (-not $applied[$k]) {
                Write-Host ("  [{0}]" -f $k)
                Write-Host ("    antes: {0}" -f $line.Trim())
                Write-Host ("    despues: {0}" -f $desired[$k]) -ForegroundColor Green
                $newLines.Add($desired[$k])
                $applied[$k] = $true
            } else {
                # Ya aplicada, comentamos duplicados
                $newLines.Add("# $line  # comentado por configure-wal-archiving.ps1 (duplicado)")
            }
            $matched = $true
            break
        }
    }
    if (-not $matched) { $newLines.Add($line) }
}

# Las que no aparecieron en el archivo, las agregamos al final
$missing = $desired.Keys | Where-Object { -not $applied[$_] }
if ($missing.Count -gt 0) {
    $newLines.Add("")
    $newLines.Add("# === Catjard: WAL archiving (agregado por configure-wal-archiving.ps1 $timestamp) ===")
    foreach ($k in $missing) {
        Write-Host ("  [{0}] no estaba -> agregado al final" -f $k) -ForegroundColor Yellow
        Write-Host ("    {0}" -f $desired[$k]) -ForegroundColor Green
        $newLines.Add($desired[$k])
    }
}

if ($DryRun) {
    Write-Host ""
    Write-Host "[dry-run] No se escribe el archivo. Resultado se mostraria arriba."
    exit 0
}

# Escribir archivo (UTF8 sin BOM para evitar problemas con el parser)
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllLines($ConfPath, $newLines, $utf8NoBom)
Write-Host "OK -> postgresql.conf actualizado"

# --- 4. Reiniciar servicio ---
Write-Section "4. Reiniciando servicio $ServiceName"
$svc = Get-Service -Name $ServiceName -ErrorAction SilentlyContinue
if (-not $svc) {
    Write-Host "AVISO: el servicio '$ServiceName' no existe en este equipo." -ForegroundColor Yellow
    Write-Host "Servicios postgres disponibles:"
    Get-Service postgres* -ErrorAction SilentlyContinue | Format-Table -AutoSize
    Write-Host "Reinicia manualmente: Restart-Service <nombre-servicio>"
} else {
    try {
        Restart-Service -Name $ServiceName -Force
        Write-Host "OK -> servicio reiniciado"
    } catch {
        Write-Host "ERROR al reiniciar (te falta admin?): $_" -ForegroundColor Red
        Write-Host "Reinicia manualmente desde una PowerShell elevada:"
        Write-Host "  Restart-Service $ServiceName"
    }
}

# --- 5. Verificacion ---
Write-Section "5. Verificacion (SHOW)"
$psql = "C:\Program Files\PostgreSQL\18\bin\psql.exe"
if (Test-Path $psql) {
    & $psql -U postgres -h localhost -p 5432 -c "SHOW wal_level; SHOW archive_mode; SHOW archive_command; SHOW max_wal_senders;"
    Write-Host ""
    Write-Host "Para forzar el primer archivado de WAL y comprobar:"
    Write-Host "  psql -U postgres -c ""SELECT pg_switch_wal(); SELECT * FROM pg_stat_archiver;"""
} else {
    Write-Host "psql no encontrado en $psql -- ejecuta los SHOW manualmente."
}

Write-Section "Hecho"
Write-Host "Backup del .conf original: $backupPath"
