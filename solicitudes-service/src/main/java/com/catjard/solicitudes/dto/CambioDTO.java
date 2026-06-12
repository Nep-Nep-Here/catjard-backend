package com.catjard.solicitudes.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CambioDTO(
        Long id,
        String codigo,
        LocalDate fecha,
        String proyecto,
        String version,
        String tipoCambio,
        String categoria,
        String titulo,
        String descripcion,
        String impacto,
        String prioridad,
        String estado,
        String etapaCI,
        String responsable,
        String solicitanteEmail,
        String areaAfectada,
        String riesgo,
        String nivelRiesgo,
        String planPruebas,
        String planRollback,
        String jiraIssueKey,
        String jiraUrl,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion
) {}
