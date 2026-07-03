package com.catjard.solicitudes.digitalocean;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

// Propiedades del monitoreo del Droplet via API de DigitalOcean (prefijo 'monitoreo.').
// Los umbrales implementan la Fase 1 (Clasificacion de Eventos): warning / error / critical.
@ConfigurationProperties(prefix = "monitoreo")
public record DigitalOceanProperties(
        @DefaultValue("true") boolean enabled,
        String apiToken,                       // Personal Access Token (read) de DigitalOcean
        String dropletId,                      // opcional: si falta, se usa el primer droplet de la cuenta
        @DefaultValue("60000") long syncIntervalMs,
        @DefaultValue("15") int cooldownMin,   // minutos sin re-alertar si la condicion sigue igual
        @DefaultValue Umbral cpu,              // % de uso
        @DefaultValue Umbral memoria,          // % de uso
        @DefaultValue Umbral disco,            // % de uso
        @DefaultValue Umbral load,             // load average 5 min
        @DefaultValue Umbral red               // Mbps (entrada y salida)
) {

    public record Umbral(
            @DefaultValue("-1") double warning,
            @DefaultValue("-1") double error,
            @DefaultValue("-1") double critical
    ) {}

    // Defaults alineados a la practica del curso y a las alertas configuradas en DO:
    //  - CPU 72% => warning, 95% => critical (ejemplos de la clase)
    //  - RAM 85% => error; Disco 95% => critical; Load 5min > 10 (alerta de DO)
    public Umbral umbralCpu()     { return conDefaults(cpu, 60, 80, 90); }
    public Umbral umbralMemoria() { return conDefaults(memoria, 70, 85, 95); }
    public Umbral umbralDisco()   { return conDefaults(disco, 80, 90, 95); }
    public Umbral umbralLoad()    { return conDefaults(load, 5, 10, 15); }
    public Umbral umbralRed()     { return conDefaults(red, 30, 50, 100); }

    private static Umbral conDefaults(Umbral u, double w, double e, double c) {
        if (u == null) return new Umbral(w, e, c);
        return new Umbral(
                u.warning() >= 0 ? u.warning() : w,
                u.error() >= 0 ? u.error() : e,
                u.critical() >= 0 ? u.critical() : c
        );
    }
}
