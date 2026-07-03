package com.catjard.solicitudes.dto;

// Lectura en vivo de una metrica del Droplet para el panel de monitoreo del front.
public record MetricaActualDTO(
        String metrica,
        Double valor,
        String unidad,
        String severidad,        // clasificacion de la lectura actual
        Double umbralWarning,
        Double umbralError,
        Double umbralCritical
) {}
