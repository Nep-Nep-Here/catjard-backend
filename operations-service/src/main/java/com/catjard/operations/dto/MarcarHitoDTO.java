package com.catjard.operations.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record MarcarHitoDTO(
        @NotBlank String hito,
        LocalDate fecha,
        String observacion
) {}
