package com.catjard.sales.client;

// Coincide con CrearMovimientoDTO de inventory-service (POST /api/movimientos).
public record CrearMovimientoBody(
        String tipo,
        Long productoId,
        Integer cantidad,
        String motivo,
        String referencia,
        String usuario,
        String notas
) {}
