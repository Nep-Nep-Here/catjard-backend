package com.catjard.catalog.dto;

import com.catjard.catalog.model.Categoria;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Set;

public record CrearProductoDTO(
        @NotBlank @Size(max = 100) String slug,
        @NotBlank String nombre,
        @NotNull Categoria categoria,
        @NotNull @DecimalMin("0.0") BigDecimal precio,
        @NotNull @Min(0) Integer stock,
        @NotNull @Min(0) Integer stockMinimo,
        String descripcion,
        String imagenUrl,
        @NotEmpty Set<String> tecnicas
) {}
