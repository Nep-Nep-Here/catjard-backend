package com.catjard.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record OCItemDTO(
        @NotNull Long productoId,
        @NotNull @Min(1) Integer cantidad,
        @NotNull BigDecimal precioUnit
) {}
