package com.catjard.solicitudes.model;

// Como se registro el respaldo: script (cron del Droplet), manual (panel),
// simulado (datos de demo del seeder) o digitalocean (backup/snapshot tomado
// por DO y sincronizado leyendo su API).
public enum OrigenRespaldo {
    script, manual, simulado, digitalocean
}
