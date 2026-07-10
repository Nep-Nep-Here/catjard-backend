package com.catjard.solicitudes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

// Registro de un riesgo (Fase 2). El nivel de riesgo NO se envia:
// el backend lo deriva de la matriz Probabilidad x Impacto.
public record CrearRiesgoDTO(
        @NotBlank @Size(max = 160) String nombre,
        String descripcion,
        @NotBlank @Size(max = 10) String probabilidad,  // bajo / medio / alto
        @NotBlank @Size(max = 10) String impacto,       // bajo / medio / alto
        String accionMitigacion,
        List<Long> servicioIds                          // servicios criticos afectados
) {}
