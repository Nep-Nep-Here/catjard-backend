package com.catjard.inventory.dto;

import java.time.LocalDate;

public record MovimientoDTO(
        Long id,
        LocalDate fecha,
        String tipo,
        Long productoId,
        Integer cantidad,
        String motivo,
        String referencia,
        String usuario,
        String notas
) {}
