package com.catjard.crm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record ActualizarClienteDTO(
        @Size(max = 200) String razonSocial,
        @Size(max = 200) String nombreComercial,
        @Size(max = 120) String industria,
        @Size(max = 150) String contactoPrincipal,
        @Email @Size(max = 150) String email,
        @Size(max = 30) String telefono,
        @Size(max = 255) String direccion,
        Boolean cuentaActiva,
        String notas
) {}
