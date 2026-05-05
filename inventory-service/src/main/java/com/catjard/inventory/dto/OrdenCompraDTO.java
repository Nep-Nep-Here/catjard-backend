package com.catjard.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record OrdenCompraDTO(
        Long id,
        String codigo,
        LocalDate fecha,
        Long proveedorId,
        String proveedorNombre,
        String estado,
        LocalDate fechaEsperada,
        LocalDate fechaRecepcion,
        BigDecimal subtotal,
        BigDecimal igv,
        BigDecimal total,
        String usuario,
        String notas,
        List<OCItemDTO> items
) {}
