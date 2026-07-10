package com.catjard.solicitudes.dto;

import jakarta.validation.constraints.Size;

import java.util.List;

// Edicion parcial de un riesgo: solo se aplican los campos no nulos.
// Si cambia probabilidad o impacto se recalcula el nivel de riesgo.
public record ActualizarRiesgoDTO(
        @Size(max = 160) String nombre,
        String descripcion,
        @Size(max = 10) String probabilidad,
        @Size(max = 10) String impacto,
        String accionMitigacion,
        @Size(max = 20) String estado,      // identificado / en_mitigacion / mitigado / aceptado
        List<Long> servicioIds              // null = no tocar; lista = reemplaza los vinculos
) {}
