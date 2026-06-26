package com.catjard.solicitudes.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record IncidenteDTO(
        Long id,
        String codigo,
        LocalDate fecha,
        String titulo,
        String descripcion,
        String origen,
        String servicioAfectado,
        String categoria,
        String impacto,
        String urgencia,
        String prioridad,
        String estado,
        String responsable,
        String solicitanteEmail,
        String diagnostico,
        String solucion,
        String evidencia,
        LocalDateTime fechaResolucion,
        LocalDateTime fechaCierre,
        String jiraIssueKey,
        String jiraUrl,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion
) {}
