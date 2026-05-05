package com.catjard.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record CrearOrdenCompraDTO(
        @NotNull Long proveedorId,
        @NotEmpty @Valid List<OCItemDTO> items,
        LocalDate fechaEsperada,
        String usuario,
        String notas
) {}
