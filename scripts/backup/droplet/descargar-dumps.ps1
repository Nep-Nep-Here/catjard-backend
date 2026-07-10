# ============================================================================
# Copia externa de la regla 3-2-1 (se ejecuta en TU PC, no en el Droplet).
#
# 1) Descarga por scp la carpeta de dumps mas reciente del Droplet.
# 2) Registra la copia externa en el modulo de Continuidad (destino
#    copia_externa) para el semaforo RPO y la trazabilidad del plan.
#
# Uso:
#   powershell -ExecutionPolicy Bypass -File descargar-dumps.ps1 `
#     -ServerIp 203.0.113.10 -Token EL_CONTINUIDAD_BACKUP_TOKEN
#
# Requiere OpenSSH en Windows (scp/ssh vienen con Windows 10/11) y la SSH key
# del Droplet cargada (o te pedira password).
# ============================================================================
param(
    [Parameter(Mandatory = $true)]  [string] $ServerIp,
    [Parameter(Mandatory = $true)]  [string] $Token,
    [string] $Usuario = "root",
    [string] $Destino = "C:\catjard-backups\droplet",
    [long]   $ServicioBdId = 1     # SRV-001 Base de datos PostgreSQL
)

$ErrorActionPreference = "Stop"

# --- 1. Ubicar la carpeta de dumps mas reciente en el Droplet ---
$ultima = (ssh "$Usuario@$ServerIp" "ls -1 ~/catjard-backups/dumps 2>/dev/null | sort | tail -1").Trim()
if (-not $ultima) {
    Write-Host "No hay dumps en el Droplet todavia (corre primero backup-diario.sh)." -ForegroundColor Yellow
    exit 1
}
Write-Host "Ultima carpeta de dumps en el Droplet: $ultima"

# --- 2. Descargar por scp ---
$carpetaLocal = Join-Path $Destino $ultima
New-Item -ItemType Directory -Force $carpetaLocal | Out-Null
$inicio = Get-Date
scp -r "${Usuario}@${ServerIp}:~/catjard-backups/dumps/$ultima/*" $carpetaLocal
if ($LASTEXITCODE -ne 0) {
    Write-Host "Fallo la descarga por scp." -ForegroundColor Red
    exit 1
}
$duracion = [int]((Get-Date) - $inicio).TotalSeconds
$tamanoMb = [math]::Round(((Get-ChildItem $carpetaLocal -Recurse | Measure-Object Length -Sum).Sum / 1MB), 1)
Write-Host "Descargado en ${carpetaLocal}: $tamanoMb MB en ${duracion}s" -ForegroundColor Green

# --- 3. Registrar la copia externa en el panel de Continuidad ---
$payload = @{
    servicioId  = $ServicioBdId
    recurso     = "Dumps $ultima (scp a PC del equipo)"
    tipo        = "completo"
    destino     = "copia_externa"
    estado      = "exitoso"
    tamanoMb    = $tamanoMb
    duracionSeg = $duracion
    mensaje     = "Copia externa de la regla 3-2-1, descargada a $env:COMPUTERNAME."
} | ConvertTo-Json

try {
    $resp = Invoke-RestMethod -Method Post -Uri "http://$ServerIp/api/continuidad/respaldos/script" `
        -Headers @{ "X-Backup-Token" = $Token } -ContentType "application/json" -Body $payload
    Write-Host "Registrado en el panel de Continuidad: $($resp.codigo)" -ForegroundColor Green
} catch {
    Write-Host "AVISO: la copia se descargo pero no se pudo registrar en el panel: $_" -ForegroundColor Yellow
}
