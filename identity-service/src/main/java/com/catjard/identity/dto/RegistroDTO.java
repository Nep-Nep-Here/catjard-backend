package com.catjard.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroDTO(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, max = 100) String password,
        @NotBlank String nombre,
        @NotBlank String empresa,
        @NotBlank @Size(min = 11, max = 11) String ruc,
        String telefono,
        String direccion
) {}
