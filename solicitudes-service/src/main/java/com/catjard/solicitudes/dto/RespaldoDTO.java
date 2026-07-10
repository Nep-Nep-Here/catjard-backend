package com.catjard.solicitudes.dto;

import java.time.LocalDateTime;

public record RespaldoDTO(
        Long id,
        String codigo,
        LocalDateTime fechaHora,
        Long servicioId,
        String servicioNombre,
        String recurso,
        String tipo,
        String destino,
        String estado,
        Double tamanoMb,
        Integer duracionSeg,
        String mensaje,
        String origen,
        LocalDateTime fechaCreacion
) {}
