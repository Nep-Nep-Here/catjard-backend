package com.catjard.solicitudes.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Alta de un servicio en el catalogo de continuidad (Fases 1 y 3).
public record CrearServicioCriticoDTO(
        @NotBlank @Size(max = 120) String nombre,
        String descripcion,
        @NotBlank @Size(max = 20) String tipo,          // microservicio / base_datos / frontend / infraestructura
        @NotBlank @Size(max = 10) String criticidad,    // baja / media / alta / critica
        @Min(1) @Max(99) Integer prioridadRecuperacion, // 1 = primero en recuperarse
        @Min(1) Integer rtoMinutos,
        @Min(1) Integer rpoMinutos,                     // null = servicio sin datos propios
        String estrategiaContinuidad
) {}
