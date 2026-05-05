package com.catjard.crm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ConvertirLeadDTO(
        @NotBlank @Size(max = 200) String razonSocial,
        @Size(max = 200) String nombreComercial,
        @NotBlank @Pattern(regexp = "\\d{11}", message = "RUC debe tener 11 digitos") String ruc,
        @Size(max = 120) String industria,
        @Size(max = 255) String direccion,
        String notas
) {}
