package com.catjard.sales.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ItemDTO(
        @NotNull Long productoId,
        @NotNull @Min(1) Integer cantidad,
        @NotNull BigDecimal precioUnit,
        String tecnica,
        String notas
) {}
