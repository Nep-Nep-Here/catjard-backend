package com.catjard.catalog.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PromocionDTO(
        Long id,
        String nombre,
        String descripcion,
        BigDecimal descuentoPct,
        LocalDate desde,
        LocalDate hasta,
        String aplicaA,
        String aplicaValor,
        Boolean activa
) {}
