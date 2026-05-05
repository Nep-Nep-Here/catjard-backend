package com.catjard.operations.dto;

import java.time.LocalDate;

public record TrackingEventoDTO(
        Long id,
        String pedidoCodigo,
        String hito,
        LocalDate fecha,
        Boolean completo,
        String observacion
) {}
