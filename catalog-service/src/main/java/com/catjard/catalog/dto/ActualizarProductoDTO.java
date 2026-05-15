package com.catjard.catalog.dto;

import com.catjard.catalog.model.Categoria;

import java.math.BigDecimal;
import java.util.Set;

public record ActualizarProductoDTO(
        String slug,
        String nombre,
        Categoria categoria,
        BigDecimal precio,
        Integer stock,
        Integer stockMinimo,
        String descripcion,
        String imagenUrl,
        Set<String> tecnicas
) {}
