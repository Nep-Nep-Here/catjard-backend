package com.catjard.identity.dto;

public record UsuarioDTO(
        Long id,
        String email,
        String role,
        String nombre,
        String empresa,
        String ruc,
        String telefono,
        String direccion,
        String cargo
) {}
