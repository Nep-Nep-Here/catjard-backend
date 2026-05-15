package com.catjard.identity.dto;

import com.catjard.identity.model.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CrearUsuarioDTO(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, max = 100) String password,
        @NotNull Rol rol,
        @NotBlank String nombre,
        String empresa,
        @Size(min = 11, max = 11) String ruc,
        String telefono,
        String direccion,
        String cargo,
        Long clienteId
) {}
