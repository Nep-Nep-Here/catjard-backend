package com.catjard.sales.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CotizacionDTO(
        Long id,
        String codigo,
        Long clienteId,
        String empresa,
        String ruc,
        LocalDate fecha,
        String logoNombre,
        String notasCliente,
        String estado,
        BigDecimal subtotal,
        BigDecimal igv,
        BigDecimal total,
        LocalDate validez,
        String notasVendedor,
        String vendedor,
        String motivoRechazo,
        String pedidoCodigo,
        List<ItemDTO> items
) {}
