package com.catjard.solicitudes.model;

// Metricas del Droplet que expone la API de Monitoring de DigitalOcean.
public enum TipoMetrica {
    cpu,          // % de uso de CPU
    memoria,      // % de uso de RAM
    disco,        // % de uso del filesystem raiz
    load,         // load average de 5 minutos
    red_entrada,  // ancho de banda publico entrante (Mbps)
    red_salida    // ancho de banda publico saliente (Mbps)
}
