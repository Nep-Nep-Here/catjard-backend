package com.catjard.solicitudes.service;

import com.catjard.solicitudes.dto.IncidenteDTO;
import com.catjard.solicitudes.mapper.IncidenteMapper;
import com.catjard.solicitudes.model.CategoriaIncidente;
import com.catjard.solicitudes.model.Incidente;
import com.catjard.solicitudes.model.MonitoreoEstado;
import com.catjard.solicitudes.model.Nivel;
import com.catjard.solicitudes.repository.MonitoreoEstadoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Deteccion de reinicios del servidor (Gestion de Continuidad): un reinicio del Droplet
 * (corte de energia, OOM, kernel panic o mantenimiento) es una interrupcion del servicio,
 * asi que abre un incidente automatico asociado a la infraestructura y activa su contador RTO.
 *
 * Como funciona: cada ciclo del scheduler lee el uptime del HOST desde /proc/uptime (dentro
 * de un contenedor Docker refleja el kernel del host, no esta aislado por namespace), calcula
 * el "boot time" (ahora - uptime) y lo compara con el ultimo conocido, que persiste en Postgres.
 * Si el boot time cambio, el servidor se reinicio. Reiniciar solo un contenedor NO lo dispara,
 * porque el uptime del host no cambia.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReinicioMonitorService {

    private static final long ESTADO_ID = 1L;
    // Entre lecturas, boot time = ahora - uptime es casi constante; un salto mayor a esto es
    // un reinicio real (no ruido de reloj/latencia).
    private static final long TOLERANCIA_MIN = 3;

    private final MonitoreoEstadoRepository estadoRepo;
    private final IncidenteService incidenteService;

    // Llamado por el scheduler de monitoreo.
    @Transactional
    public Optional<Incidente> detectarReinicio() {
        Long uptimeSeg = leerUptimeHostSeg();
        if (uptimeSeg == null) return Optional.empty();   // no Linux / no legible: no aplica
        return procesarBootTime(LocalDateTime.now().minusSeconds(uptimeSeg), uptimeSeg);
    }

    // Logica pura (testeable): decide si el boot time observado indica un reinicio nuevo.
    Optional<Incidente> procesarBootTime(LocalDateTime bootActual, long uptimeSeg) {
        MonitoreoEstado estado = estadoRepo.findById(ESTADO_ID).orElse(null);

        if (estado == null) {
            // Primera vez (o BD recien sembrada): fija la linea base, sin generar incidente.
            estadoRepo.save(MonitoreoEstado.builder()
                    .id(ESTADO_ID).ultimoArranque(bootActual)
                    .fechaActualizacion(LocalDateTime.now()).build());
            log.info("Monitoreo reinicio: linea base fijada (arranque {}).", bootActual);
            return Optional.empty();
        }

        long deltaMin = Duration.between(estado.getUltimoArranque(), bootActual).abs().toMinutes();
        if (deltaMin < TOLERANCIA_MIN) return Optional.empty();   // mismo arranque: nada que hacer

        // Boot time nuevo => el servidor se reinicio.
        Incidente inc = incidenteService.crearDesdeMonitoreo(
                "El servidor se reinicio (posible corte, OOM o mantenimiento)",
                "El monitoreo detecto que el Droplet se reinicio.\n"
                        + "Arranque anterior: " + estado.getUltimoArranque() + "\n"
                        + "Nuevo arranque: " + bootActual + "\n"
                        + "Uptime actual: " + (uptimeSeg / 60) + " min.\n\n"
                        + "Los contenedores se relevantan solos (restart: unless-stopped). "
                        + "Verificar que todos los servicios esten arriba (docker compose ps) e "
                        + "investigar la causa (last reboot, journalctl -b -1).",
                CategoriaIncidente.infraestructura,
                Nivel.alto, Nivel.alto);

        estado.setUltimoArranque(bootActual);
        estado.setFechaActualizacion(LocalDateTime.now());
        estadoRepo.save(estado);
        log.warn("Monitoreo reinicio: DETECTADO reinicio del Droplet -> incidente {}.", inc.getCodigo());
        return Optional.of(inc);
    }

    // Simulacion para demos: abre el incidente de reinicio SIN reiniciar el servidor y SIN
    // tocar el estado real del boot time (no interfiere con la deteccion automatica en curso).
    @Transactional
    public IncidenteDTO simularReinicio() {
        Incidente inc = incidenteService.crearDesdeMonitoreo(
                "[SIMULACION] El servidor se reinicio",
                "Reinicio SIMULADO desde el panel (demostracion): el servidor NO se reinicio "
                        + "realmente.\n\nEn un reinicio real, el monitoreo lee el uptime del host "
                        + "(/proc/uptime), detecta que el arranque cambio y abre este mismo incidente "
                        + "asociado a Infraestructura, activando su contador RTO.",
                CategoriaIncidente.infraestructura,
                Nivel.alto, Nivel.alto);
        log.info("Simulacion de reinicio -> incidente {}.", inc.getCodigo());
        return IncidenteMapper.toDTO(inc);
    }

    // Uptime del HOST en segundos desde /proc/uptime. Null si no existe (Windows/local) o falla.
    Long leerUptimeHostSeg() {
        Path p = Path.of("/proc/uptime");
        if (!Files.isReadable(p)) return null;
        try {
            String contenido = Files.readString(p).trim();   // "12345.67 8901.23"
            return (long) Double.parseDouble(contenido.split("\\s+")[0]);
        } catch (Exception e) {
            log.debug("No se pudo leer /proc/uptime: {}", e.getMessage());
            return null;
        }
    }
}
