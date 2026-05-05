package com.catjard.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CrearMovimientoDTO(
        @NotBlank String tipo,
        @NotNull Long productoId,
        @NotNull Integer cantidad,
        @NotBlank @Size(max = 120) String motivo,
        @Size(max = 40) String referencia,
        @Size(max = 120) String usuario,
        String notas
) {}
