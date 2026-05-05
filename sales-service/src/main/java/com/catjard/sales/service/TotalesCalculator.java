package com.catjard.sales.service;

import com.catjard.sales.dto.ItemDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class TotalesCalculator {

    public static final BigDecimal IGV_RATE = new BigDecimal("0.18");

    public record Totales(BigDecimal subtotal, BigDecimal igv, BigDecimal total) {}

    private TotalesCalculator() {}

    public static Totales calcular(List<ItemDTO> items) {
        BigDecimal subtotal = items.stream()
                .map(it -> it.precioUnit().multiply(BigDecimal.valueOf(it.cantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal igv = subtotal.multiply(IGV_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(igv).setScale(2, RoundingMode.HALF_UP);
        return new Totales(subtotal, igv, total);
    }
}
