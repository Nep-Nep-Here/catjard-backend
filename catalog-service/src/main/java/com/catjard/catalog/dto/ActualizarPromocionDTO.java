package com.catjard.catalog.dto;

import com.catjard.catalog.model.AplicaA;
import com.catjard.catalog.model.Categoria;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ActualizarPromocionDTO(
        String nombre,
        String descripcion,
        BigDecimal descuentoPct,
        LocalDate desde,
        LocalDate hasta,
        AplicaA aplicaA,
        Categoria categoriaAplica,
        Long productoId,
        Boolean activa
) {}
