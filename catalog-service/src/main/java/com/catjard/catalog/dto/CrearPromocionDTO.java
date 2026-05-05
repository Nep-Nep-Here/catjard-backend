package com.catjard.catalog.dto;

import com.catjard.catalog.model.AplicaA;
import com.catjard.catalog.model.Categoria;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CrearPromocionDTO(
        @NotBlank String nombre,
        String descripcion,
        @NotNull @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal descuentoPct,
        @NotNull LocalDate desde,
        @NotNull LocalDate hasta,
        @NotNull AplicaA aplicaA,
        Categoria categoriaAplica,
        Long productoId,
        @NotNull Boolean activa
) {}
