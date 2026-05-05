package com.catjard.crm.dto;

import java.time.LocalDate;

public record LeadDTO(
        Long id,
        String codigo,
        LocalDate fecha,
        String nombre,
        String empresa,
        String ruc,
        String email,
        String telefono,
        String productos,
        String cantidad,
        String mensaje,
        String estado,
        String asignadoA,
        String notasInternas
) {}
