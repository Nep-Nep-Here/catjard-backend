package com.catjard.crm.dto;

import java.time.LocalDate;

public record ClienteDTO(
        Long id,
        String razonSocial,
        String nombreComercial,
        String ruc,
        String industria,
        String contactoPrincipal,
        String email,
        String telefono,
        String direccion,
        Boolean cuentaActiva,
        LocalDate fechaAlta,
        String notas
) {}
