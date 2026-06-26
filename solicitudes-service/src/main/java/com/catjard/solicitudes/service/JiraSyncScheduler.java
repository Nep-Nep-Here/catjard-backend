package com.catjard.solicitudes.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// Sincroniza periodicamente el estado de las solicitudes con Jira (Jira -> backend).
// Asi, cuando mueves el issue en Jira, el sistema lo refleja sin intervencion manual.
@Slf4j
@Component
@RequiredArgsConstructor
public class JiraSyncScheduler {

    private final SolicitudService service;
    private final CambioService cambioService;
    private final IncidenteService incidenteService;

    @Scheduled(initialDelay = 20_000, fixedDelayString = "${jira.sync-interval-ms:30000}")
    public void sincronizar() {
        try {
            int n = service.sincronizarConJira();
            if (n > 0) log.info("Sync Jira programado: {} solicitud(es) actualizada(s).", n);
        } catch (Exception ex) {
            log.warn("Sync Jira programado fallo: {}", ex.getMessage());
        }
        try {
            int n = cambioService.sincronizarConJira();
            if (n > 0) log.info("Sync Jira programado: {} cambio(s) actualizado(s).", n);
        } catch (Exception ex) {
            log.warn("Sync Jira (cambios) programado fallo: {}", ex.getMessage());
        }
        try {
            int n = incidenteService.sincronizarConJira();
            if (n > 0) log.info("Sync Jira programado: {} incidente(s) actualizado(s).", n);
        } catch (Exception ex) {
            log.warn("Sync Jira (incidentes) programado fallo: {}", ex.getMessage());
        }
    }
}
