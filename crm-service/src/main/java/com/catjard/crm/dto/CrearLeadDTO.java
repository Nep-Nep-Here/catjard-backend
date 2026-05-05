package com.catjard.crm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrearLeadDTO(
        @NotBlank @Size(max = 150) String nombre,
        @Size(max = 150) String empresa,
        @Size(max = 11) String ruc,
        @NotBlank @Email @Size(max = 150) String email,
        @Size(max = 30) String telefono,
        String productos,
        @Size(max = 50) String cantidad,
        String mensaje
) {}
