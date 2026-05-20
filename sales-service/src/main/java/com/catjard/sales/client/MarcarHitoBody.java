package com.catjard.sales.client;

import java.time.LocalDate;

// Coincide con MarcarHitoDTO de operations-service (POST /api/tracking/pedido/{codigo}).
public record MarcarHitoBody(String hito, LocalDate fecha, String observacion) {}
