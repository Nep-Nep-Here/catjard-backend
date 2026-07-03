package com.catjard.solicitudes.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// Monitoreo proactivo del Droplet: cada intervalo lee las metricas reales via la API
// de DigitalOcean, clasifica cada lectura y registra los eventos (DigitalOcean -> backend).
// Asi el panel de eventos del front se actualiza solo, sin depender del correo de alertas.
@Slf4j
@Component
@RequiredArgsConstructor
public class MonitoreoScheduler {

    private final EventoService eventoService;

    @Scheduled(initialDelay = 30_000, fixedDelayString = "${monitoreo.sync-interval-ms:60000}")
    public void monitorear() {
        try {
            int n = eventoService.evaluarMetricas();
            if (n > 0) log.info("Monitoreo DO: {} evento(s) nuevo(s) registrado(s).", n);
        } catch (Exception ex) {
            log.warn("Monitoreo DO programado fallo: {}", ex.getMessage());
        }
    }
}
