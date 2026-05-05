package com.catjard.catalog.dto;

import jakarta.validation.constraints.Min;

public record ActualizarStockDTO(
        @Min(0) Integer stock,
        Integer delta
) {}
