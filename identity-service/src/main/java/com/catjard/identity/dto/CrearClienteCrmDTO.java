package com.catjard.identity.dto;

// Payload que identity envía a crm-service (POST /api/clientes/registro) al
// auto-registrarse un cliente. Coincide con el CrearClienteDTO de crm-service.
public record CrearClienteCrmDTO(
        String razonSocial,
        String nombreComercial,
        String ruc,
        String industria,
        String contactoPrincipal,
        String email,
        String telefono,
        String direccion,
        Boolean cuentaActiva,
        String notas
) {}
