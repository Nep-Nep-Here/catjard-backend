package com.catjard.catalog.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductoDTO(
        Long id,
        String slug,
        String nombre,
        String categoria,
        BigDecimal precio,
        Integer stock,
        Integer stockMinimo,
        String descripcion,
        String imagenUrl,
        List<String> tecnicas
) {}
