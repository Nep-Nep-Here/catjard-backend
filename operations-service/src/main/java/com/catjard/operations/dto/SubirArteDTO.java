package com.catjard.operations.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubirArteDTO(
        @NotBlank @Size(max = 255) String nombreArchivo
) {}
