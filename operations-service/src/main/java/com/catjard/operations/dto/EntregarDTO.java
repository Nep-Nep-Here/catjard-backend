package com.catjard.operations.dto;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record EntregarDTO(
        LocalDate fechaEntregaReal,
        @Size(max = 150) String receptor,
        String notas
) {}
