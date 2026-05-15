# cleanup-old-backups.ps1
# Aplica politica de retencion sobre dumps, basebackups y WAL archive.
#
# Politica por defecto:
#   - dumps     : conservar 14 dias
#   - basebackup: conservar 4 ejecuciones (mas o menos 1 mes)
#   - wal       : conservar 7 dias (suficiente cubriendo el ultimo basebackup)
#
# OJO con WAL: nunca borrar WAL anterior al basebackup mas viejo que aun
# quieras restaurar; sino pierdes la cadena para PITR.

$ErrorActionPreference = "Stop"

# === CONFIG ===
$BackupRoot   = "C:\catjard-backups"
$DumpDir      = Join-Path $BackupRoot "dumps"
$BaseDir      = Join-Path $BackupRoot "basebackup"
$WalDir       = Join-Path $BackupRoot "wal_archive"
$LogDir       = Join-Path $BackupRoot "logs"

$DumpRetentionDays = 14
$WalRetentionDays  = 7
$BaseKeepCount     = 4

# === SETUP ===
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$logFile   = Join-Path $LogDir "cleanup_$timestamp.log"
if (!(Test-Path $LogDir)) { New-Item -ItemType Directory -Path $LogDir -Force | Out-Null }

function Write-Log {
    param([string]$msg)
    $line = "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] $msg"
    Write-Output $line
    Add-Content -Path $logFile -Value $line
}

Write-Log "=== Inicio cleanup ==="

# --- 1. Dumps por antiguedad ---
if (Test-Path $DumpDir) {
    $cutoff = (Get-Date).AddDays(-$DumpRetentionDays)
    $oldDumps = Get-ChildItem -Path $DumpDir -Filter "*.dump" |
                Where-Object { $_.LastWriteTime -lt $cutoff }
    Write-Log "Dumps a borrar (anteriores a $cutoff): $($oldDumps.Count)"
    foreach ($f in $oldDumps) {
        Write-Log "  rm $($f.Name)"
        Remove-Item -Path $f.FullName -Force
    }
}

# --- 2. Basebackups: conservar los N mas recientes (carpetas) ---
if (Test-Path $BaseDir) {
    $allBases = Get-ChildItem -Path $BaseDir -Directory | Sort-Object LastWriteTime -Descending
    if ($allBases.Count -gt $BaseKeepCount) {
        $toRemove = $allBases | Select-Object -Skip $BaseKeepCount
        Write-Log "Basebackups a borrar (manteniendo $BaseKeepCount): $($toRemove.Count)"
        foreach ($d in $toRemove) {
            Write-Log "  rm -r $($d.Name)"
            Remove-Item -Path $d.FullName -Recurse -Force
        }
    } else {
        Write-Log "Basebackups: $($allBases.Count) <= $BaseKeepCount, nada que borrar"
    }
}

# --- 3. WAL archive por antiguedad ---
if (Test-Path $WalDir) {
    $cutoff = (Get-Date).AddDays(-$WalRetentionDays)
    $oldWal = Get-ChildItem -Path $WalDir -File |
              Where-Object { $_.LastWriteTime -lt $cutoff }
    Write-Log "WAL a borrar (anteriores a $cutoff): $($oldWal.Count)"
    foreach ($f in $oldWal) {
        Remove-Item -Path $f.FullName -Force
    }
}

# --- 4. Logs viejos del propio cleanup ---
if (Test-Path $LogDir) {
    $cutoff = (Get-Date).AddDays(-30)
    Get-ChildItem -Path $LogDir -File |
      Where-Object { $_.LastWriteTime -lt $cutoff } |
      ForEach-Object { Remove-Item $_.FullName -Force }
}

Write-Log "=== Fin cleanup ==="
