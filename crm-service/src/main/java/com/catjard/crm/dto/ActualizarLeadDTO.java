package com.catjard.crm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record ActualizarLeadDTO(
        @Size(max = 150) String nombre,
        @Size(max = 150) String empresa,
        @Size(max = 11) String ruc,
        @Email @Size(max = 150) String email,
        @Size(max = 30) String telefono,
        String productos,
        @Size(max = 50) String cantidad,
        String mensaje,
        String estado,
        @Size(max = 120) String asignadoA,
        String notasInternas
) {}
