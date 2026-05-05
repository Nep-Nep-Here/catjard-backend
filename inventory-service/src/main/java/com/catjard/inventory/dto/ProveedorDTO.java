package com.catjard.inventory.dto;

import java.time.LocalDate;

public record ProveedorDTO(
        Long id,
        String razonSocial,
        String nombreComercial,
        String ruc,
        String contacto,
        String email,
        String telefono,
        String direccion,
        String productos,
        String notas,
        Boolean activo,
        LocalDate fechaAlta
) {}
