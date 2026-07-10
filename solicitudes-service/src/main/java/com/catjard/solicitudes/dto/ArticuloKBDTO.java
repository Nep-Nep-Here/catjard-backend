package com.catjard.solicitudes.dto;

import java.time.LocalDateTime;

public record ArticuloKBDTO(
        Long id,
        String codigo,
        String titulo,
        String categoria,
        String resumen,
        String contenido,
        String autor,
        int vistas,
        String categoriaIncidente,
        Long servicioId,
        String servicioNombre,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion
) {}
