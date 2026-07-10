package com.catjard.solicitudes.model;

// Destino de la copia segun la regla 3-2-1:
// droplet_local (copia 1, mismo servidor), snapshot_do (copia 2, imagen del Droplet
// en DigitalOcean) y copia_externa (copia 3, fuera del proveedor: PC del equipo).
public enum DestinoRespaldo {
    droplet_local,
    snapshot_do,
    copia_externa
}
