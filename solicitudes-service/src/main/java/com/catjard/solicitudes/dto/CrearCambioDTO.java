package com.catjard.solicitudes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Datos del formulario "Plan de Control de Cambios" al solicitar un cambio.
public record CrearCambioDTO(
        @Size(max = 160) String proyecto,
        @NotBlank @Size(max = 15) String tipoCambio,     // estandar / normal / emergencia
        @NotBlank @Size(max = 20) String categoria,      // infraestructura / aplicaciones / documentacion / calendario
        @NotBlank @Size(max = 160) String titulo,        // cambio solicitado (resumen)
        String descripcion,
        @Size(max = 10) String impacto,                  // bajo / medio / alto
        @Size(max = 10) String prioridad,                // baja / media / alta
        @Size(max = 150) String responsable,
        @Size(max = 200) String areaAfectada,
        String riesgo,
        @Size(max = 10) String nivelRiesgo,
        String planPruebas,
        String planRollback
) {}
