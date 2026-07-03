package com.catjard.solicitudes.dto;

import java.time.LocalDateTime;

public record EventoDTO(
        Long id,
        String codigo,
        LocalDateTime fechaHora,
        String origen,
        String droplet,
        String metrica,
        Double valor,
        String unidad,
        Double umbral,
        String severidad,
        String mensaje,
        boolean generaIncidente,
        String justificacion,
        Long incidenteId,
        String incidenteCodigo,
        String accionRecomendada,
        String responsable,
        String tiempoMaximo,
        String jiraIssueKey,
        String jiraUrl,
        String estado,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion
) {}
