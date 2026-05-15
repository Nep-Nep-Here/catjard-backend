package com.catjard.crm.dto;

// Resultado de convertir un lead en cliente CRM.
// Si se creó cuenta de acceso, incluye email + passwordTemporal (este último solo se
// devuelve en claro UNA vez; el vendedor debe entregárselo al cliente).
public record ConversionResultDTO(
        Long leadId,
        Long clienteId,
        String email,
        String passwordTemporal,
        boolean cuentaCreada,
        String mensaje
) {}
