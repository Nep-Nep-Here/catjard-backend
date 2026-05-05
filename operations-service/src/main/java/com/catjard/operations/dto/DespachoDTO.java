package com.catjard.operations.dto;

import java.time.LocalDate;

public record DespachoDTO(
        Long id,
        String pedidoCodigo,
        String courier,
        String guiaRemision,
        LocalDate fechaDespacho,
        LocalDate fechaEntregaReal,
        String direccionEntrega,
        String receptor,
        String notas
) {}
