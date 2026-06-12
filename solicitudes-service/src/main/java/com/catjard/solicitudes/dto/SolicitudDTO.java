package com.catjard.solicitudes.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SolicitudDTO(
        Long id,
        String codigo,
        LocalDate fecha,
        String tipo,
        String asunto,
        String descripcion,
        String prioridad,
        String estado,
        String solicitanteEmail,
        String solicitanteNombre,
        String respuesta,
        String atendidoPor,
        String jiraIssueKey,
        String jiraUrl,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion
) {}
