package com.catjard.sales.dto;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ActualizarPedidoDTO(
        String estado,
        LocalDate fechaEntregaEstimada,
        @Size(max = 255) String voucherUrl,
        LocalDate voucherFecha,
        @Size(max = 60) String courier,
        @Size(max = 60) String guiaRemision
) {}
