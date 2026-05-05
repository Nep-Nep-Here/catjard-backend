package com.catjard.identity.dto;

public record LoginResponse(
        String token,
        long expiresInMs,
        UsuarioDTO user
) {}
