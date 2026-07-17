package com.catjard.sales.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PedidoDTO(
        Long id,
        String codigo,
        String cotizacionCodigo,
        Long clienteId,
        String empresa,
        LocalDate fechaPedido,
        LocalDate fechaEntregaEstimada,
        BigDecimal subtotal,
        BigDecimal igv,
        BigDecimal total,
        String voucherUrl,
        LocalDate voucherFecha,
        String estado,
        String courier,
        String guiaRemision,
        String procesadoPor,
        List<ItemDTO> items
) {}
