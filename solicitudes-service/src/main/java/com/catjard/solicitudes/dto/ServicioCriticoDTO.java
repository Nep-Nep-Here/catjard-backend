package com.catjard.solicitudes.dto;

import java.time.LocalDateTime;

public record ServicioCriticoDTO(
        Long id,
        String codigo,
        String nombre,
        String descripcion,
        String tipo,
        String criticidad,
        Integer prioridadRecuperacion,
        Integer rtoMinutos,
        Integer rpoMinutos,
        String estrategiaContinuidad,
        boolean activo,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion
) {}
