package com.catjard.sales.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record CrearCotizacionDTO(
        @NotNull Long clienteId,
        @NotBlank @Size(max = 200) String empresa,
        @Size(max = 11) String ruc,
        @NotEmpty @Valid List<ItemDTO> items,
        String logoNombre,
        String notasCliente,
        LocalDate validez,
        String notasVendedor,
        String vendedor
) {}
