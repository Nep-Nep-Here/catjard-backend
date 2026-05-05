package com.catjard.inventory.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CrearProveedorDTO(
        @NotBlank @Size(max = 200) String razonSocial,
        @Size(max = 200) String nombreComercial,
        @NotBlank @Pattern(regexp = "\\d{11}", message = "RUC debe tener 11 digitos") String ruc,
        @Size(max = 150) String contacto,
        @Email @Size(max = 150) String email,
        @Size(max = 30) String telefono,
        @Size(max = 255) String direccion,
        String productos,
        String notas,
        Boolean activo
) {}
