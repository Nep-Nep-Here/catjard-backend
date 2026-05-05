package com.catjard.identity.dto;

public record ActualizarUsuarioDTO(
        String nombre,
        String empresa,
        String ruc,
        String telefono,
        String direccion,
        String cargo
) {}
