package com.catjard.operations.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CrearDespachoDTO(
        @NotBlank @Size(max = 20) String pedidoCodigo,
        @NotBlank String courier,
        @Size(max = 60) String guiaRemision,
        LocalDate fechaDespacho,
        @Size(max = 255) String direccionEntrega,
        String notas
) {}
