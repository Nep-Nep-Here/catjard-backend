package com.catjard.sales.dto;

import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;

public record ActualizarCotizacionDTO(
        String estado,
        String logoNombre,
        String notasCliente,
        LocalDate validez,
        String notasVendedor,
        String vendedor,
        String motivoRechazo,
        @Valid List<ItemDTO> items
) {}
