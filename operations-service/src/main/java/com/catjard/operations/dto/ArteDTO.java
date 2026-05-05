package com.catjard.operations.dto;

import java.time.LocalDate;

public record ArteDTO(
        Long id,
        String pedidoCodigo,
        Integer version,
        String nombreArchivo,
        LocalDate fecha,
        String estado,
        String comentariosCliente
) {}
