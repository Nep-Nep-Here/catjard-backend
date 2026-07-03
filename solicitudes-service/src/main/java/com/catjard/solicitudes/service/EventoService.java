package com.catjard.solicitudes.service;

import com.catjard.solicitudes.digitalocean.DigitalOceanProperties;
import com.catjard.solicitudes.digitalocean.DigitalOceanService;
import com.catjard.solicitudes.dto.ActualizarEventoDTO;
import com.catjard.solicitudes.dto.EventoDTO;
import com.catjard.solicitudes.dto.MetricaActualDTO;
import com.catjard.solicitudes.dto.SimularEventoDTO;
import com.catjard.solicitudes.jira.JiraService;
import com.catjard.solicitudes.mapper.EventoMapper;
import com.catjard.solicitudes.model.*;
import com.catjard.solicitudes.repository.EventoRepository;
import com.catjard.solicitudes.repository.IncidenteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Monitoreo Estrategico y Gestion de Eventos.
// Pipeline por lectura de metrica:
//   1) Deteccion    : API de Monitoring de DigitalOcean (o simulacion).
//   2) Clasificacion: severidad por umbrales (informacion / warning / error / critical).
//   3) Identificacion: ¿genera incidente? error/critical => se auto-crea el incidente.
//   4) Plan de respuesta: accion, responsable y tiempo maximo autodesignados.
//   5) Escalamiento opcional: boton "Enviar a Jira" (tablero GDICJ) para los criticos.
@Slf4j
@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository repo;
    private final IncidenteRepository incidenteRepo;
    private final IncidenteService incidenteService;
    private final DigitalOceanService digitalOcean;
    private final DigitalOceanProperties props;
    private final JiraService jira;

    // Resultado de la Fase 1: severidad + umbral cruzado.
    private record Clasificacion(SeveridadEvento severidad, Double umbral) {}

    // ------------------- consulta -------------------

    @Transactional(readOnly = true)
    public List<EventoDTO> listar(Optional<String> severidad, Optional<String> estado) {
        List<Evento> data;
        if (severidad.isPresent() && !severidad.get().isBlank()) {
            data = repo.findBySeveridadOrderByFechaHoraDescIdDesc(parseSeveridad(severidad.get()));
        } else if (estado.isPresent() && !estado.get().isBlank()) {
            data = repo.findByEstadoOrderByFechaHoraDescIdDesc(parseEstado(estado.get()));
        } else {
            data = repo.findAllByOrderByFechaHoraDescIdDesc();
        }
        return data.stream().map(EventoMapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public EventoDTO obtener(Long id) {
        return repo.findById(id).map(EventoMapper::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado: " + id));
    }

    // ------------------- deteccion (scheduler / sync manual) -------------------

    // Lee todas las metricas del Droplet y pasa cada lectura por el pipeline.
    // Devuelve cuantos eventos nuevos se registraron.
    @Transactional
    public int evaluarMetricas() {
        if (!digitalOcean.isEnabled()) return 0;

        DigitalOceanService.Droplet droplet = digitalOcean.obtenerDroplet();
        if (droplet == null) {
            log.warn("Monitoreo DO: no se encontro ningun droplet en la cuenta.");
            return 0;
        }

        int creados = 0;
        for (TipoMetrica metrica : TipoMetrica.values()) {
            try {
                Double valor = leerMetrica(droplet.id(), metrica);
                if (valor == null) continue;
                if (procesarLectura(metrica, valor, "digitalocean", droplet.nombre(), true).isPresent()) {
                    creados++;
                }
            } catch (Exception ex) {
                log.warn("Monitoreo DO: fallo leyendo {}: {}", metrica, ex.getMessage());
            }
        }
        return creados;
    }

    // Simulacion para demos: misma clasificacion y mismo flujo, sin cooldown.
    @Transactional
    public EventoDTO simular(SimularEventoDTO dto) {
        TipoMetrica metrica = parseMetrica(dto.metrica());
        Evento e = procesarLectura(metrica, dto.valor(), "simulado", "simulacion", false)
                .orElseThrow(() -> new IllegalStateException("La lectura simulada no genero evento."));
        return EventoMapper.toDTO(e);
    }

    // Lecturas en vivo para el panel (gauges de CPU / RAM / disco / load / red).
    public List<MetricaActualDTO> metricasActuales() {
        if (!digitalOcean.isEnabled()) return List.of();
        DigitalOceanService.Droplet droplet = digitalOcean.obtenerDroplet();
        if (droplet == null) return List.of();

        List<MetricaActualDTO> out = new ArrayList<>();
        for (TipoMetrica metrica : TipoMetrica.values()) {
            try {
                Double valor = leerMetrica(droplet.id(), metrica);
                if (valor == null) continue;
                Clasificacion c = clasificar(metrica, valor);
                DigitalOceanProperties.Umbral u = umbralDe(metrica);
                out.add(new MetricaActualDTO(metrica.name(), valor, unidadDe(metrica),
                        c.severidad().name(), u.warning(), u.error(), u.critical()));
            } catch (Exception ex) {
                log.warn("Monitoreo DO: fallo leyendo {}: {}", metrica, ex.getMessage());
            }
        }
        return out;
    }

    // Politicas de alerta configuradas en DigitalOcean (se muestran tal cual en el front).
    public List<Map<String, Object>> alertasDigitalOcean() {
        if (!digitalOcean.isEnabled()) return List.of();
        return digitalOcean.politicasAlerta();
    }

    // ------------------- pipeline central -------------------

    // Clasifica la lectura y decide si registrar un evento. Anti-ruido: si la condicion
    // sigue igual que el ultimo evento de la metrica (dentro del cooldown) no duplica;
    // una lectura normal solo se registra como "recuperacion" tras una alerta.
    @Transactional
    protected Optional<Evento> procesarLectura(TipoMetrica metrica, double valor,
                                               String origen, String droplet, boolean conCooldown) {
        Clasificacion c = clasificar(metrica, valor);
        Optional<Evento> ultimo = repo.findTopByMetricaOrderByIdDesc(metrica);

        if (c.severidad() == SeveridadEvento.informacion) {
            boolean recuperacion = ultimo.isPresent()
                    && ultimo.get().getSeveridad() != SeveridadEvento.informacion;
            if (!recuperacion) return Optional.empty();
        } else if (conCooldown && ultimo.isPresent()) {
            Evento u = ultimo.get();
            boolean mismaCondicion = u.getSeveridad() == c.severidad()
                    && u.getFechaHora().isAfter(LocalDateTime.now().minusMinutes(props.cooldownMin()));
            if (mismaCondicion) return Optional.empty();
        }

        boolean generaIncidente = c.severidad() == SeveridadEvento.alto
                || c.severidad() == SeveridadEvento.critico;

        Evento e = Evento.builder()
                .codigo(generarCodigo())
                .fechaHora(LocalDateTime.now())
                .origen(origen)
                .droplet(droplet)
                .metrica(metrica)
                .valor(valor)
                .unidad(unidadDe(metrica))
                .umbral(c.umbral())
                .severidad(c.severidad())
                .mensaje(mensajeDe(metrica, valor, c))
                .generaIncidente(generaIncidente)
                .justificacion(justificacionDe(metrica, valor, c))
                .accionRecomendada(accionDe(metrica, c.severidad()))
                .responsable(responsableDe(c.severidad()))
                .tiempoMaximo(tiempoMaximoDe(c.severidad()))
                .estado(EstadoEvento.nuevo)
                .build();

        if (generaIncidente) {
            Incidente inc = incidenteService.crearDesdeMonitoreo(
                    "[MONITOREO] " + e.getMensaje(),
                    "Evento " + e.getCodigo() + " detectado por monitoreo (" + origen + ").\n"
                            + "Droplet: " + droplet
                            + "\nMetrica: " + metrica.name()
                            + "\nValor: " + valor + " " + e.getUnidad()
                            + "\nUmbral cruzado: " + c.umbral() + " (" + c.severidad().name() + ")\n\n"
                            + e.getJustificacion(),
                    categoriaDe(metrica),
                    Nivel.alto,
                    c.severidad() == SeveridadEvento.critico ? Nivel.alto : Nivel.medio);
            e.setIncidenteId(inc.getId());
            e.setIncidenteCodigo(inc.getCodigo());
        }

        Evento saved = repo.save(e);
        log.info("Evento {} registrado: {} [{}]{}", saved.getCodigo(), saved.getMensaje(),
                saved.getSeveridad(), saved.isGeneraIncidente() ? " -> incidente " + saved.getIncidenteCodigo() : "");
        return Optional.of(saved);
    }

    // ------------------- escalamiento a Jira (boton del panel) -------------------

    // Envia el incidente vinculado al tablero GDICJ. Pensado para los eventos criticos:
    // el operador decide desde el panel de eventos.
    @Transactional
    public EventoDTO enviarAJira(Long id) {
        Evento e = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado: " + id));
        if (e.getIncidenteId() == null) {
            throw new IllegalStateException("El evento " + e.getCodigo()
                    + " no genero incidente (severidad " + e.getSeveridad() + "): no se envia a Jira.");
        }
        if (e.getJiraIssueKey() != null && !e.getJiraIssueKey().isBlank()) {
            throw new IllegalStateException("El evento " + e.getCodigo()
                    + " ya fue enviado a Jira (" + e.getJiraIssueKey() + ").");
        }

        Incidente inc = incidenteRepo.findById(e.getIncidenteId())
                .orElseThrow(() -> new IllegalStateException("Incidente vinculado no encontrado: " + e.getIncidenteId()));

        JiraService.JiraIssue issue = jira.crearIssue(inc);
        if (issue == null) {
            throw new IllegalStateException("La integracion con Jira esta deshabilitada.");
        }
        inc.setJiraIssueKey(issue.key());
        inc.setJiraUrl(issue.url());
        e.setJiraIssueKey(issue.key());
        e.setJiraUrl(issue.url());
        if (e.getEstado() == EstadoEvento.nuevo) e.setEstado(EstadoEvento.en_revision);
        return EventoMapper.toDTO(e);
    }

    // ------------------- gestion en el panel -------------------

    @Transactional
    public EventoDTO actualizar(Long id, ActualizarEventoDTO dto) {
        Evento e = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado: " + id));
        if (dto.estado() != null && !dto.estado().isBlank()) e.setEstado(parseEstado(dto.estado()));
        if (dto.responsable() != null) e.setResponsable(dto.responsable());
        if (dto.accionRecomendada() != null) e.setAccionRecomendada(dto.accionRecomendada());
        if (dto.tiempoMaximo() != null) e.setTiempoMaximo(dto.tiempoMaximo());
        if (dto.justificacion() != null) e.setJustificacion(dto.justificacion());
        return EventoMapper.toDTO(e);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!repo.existsById(id)) throw new IllegalArgumentException("Evento no encontrado: " + id);
        repo.deleteById(id);
    }

    // ------------------- Fase 1: clasificacion por umbrales -------------------

    private Clasificacion clasificar(TipoMetrica metrica, double valor) {
        DigitalOceanProperties.Umbral u = umbralDe(metrica);
        if (valor >= u.critical()) return new Clasificacion(SeveridadEvento.critico, u.critical());
        if (valor >= u.error())    return new Clasificacion(SeveridadEvento.alto, u.error());
        if (valor >= u.warning())  return new Clasificacion(SeveridadEvento.advertencia, u.warning());
        return new Clasificacion(SeveridadEvento.informacion, null);
    }

    private DigitalOceanProperties.Umbral umbralDe(TipoMetrica m) {
        return switch (m) {
            case cpu -> props.umbralCpu();
            case memoria -> props.umbralMemoria();
            case disco -> props.umbralDisco();
            case load -> props.umbralLoad();
            case red_entrada, red_salida -> props.umbralRed();
        };
    }

    private Double leerMetrica(String hostId, TipoMetrica m) {
        return switch (m) {
            case cpu -> digitalOcean.cpuPorcentaje(hostId);
            case memoria -> digitalOcean.memoriaPorcentaje(hostId);
            case disco -> digitalOcean.discoPorcentaje(hostId);
            case load -> digitalOcean.load5(hostId);
            case red_entrada -> digitalOcean.anchoBandaMbps(hostId, "inbound");
            case red_salida -> digitalOcean.anchoBandaMbps(hostId, "outbound");
        };
    }

    private String unidadDe(TipoMetrica m) {
        return switch (m) {
            case cpu, memoria, disco -> "%";
            case load -> "load";
            case red_entrada, red_salida -> "Mbps";
        };
    }

    private String nombreDe(TipoMetrica m) {
        return switch (m) {
            case cpu -> "CPU";
            case memoria -> "Memoria RAM";
            case disco -> "Disco";
            case load -> "Load average (5 min)";
            case red_entrada -> "Trafico de red entrante";
            case red_salida -> "Trafico de red saliente";
        };
    }

    private String mensajeDe(TipoMetrica m, double valor, Clasificacion c) {
        String base = nombreDe(m) + " en " + valor + " " + unidadDe(m);
        if (c.severidad() == SeveridadEvento.informacion) {
            return base + " - valor normal (recuperado)";
        }
        return base + " (umbral " + c.severidad().name() + ": " + c.umbral() + " " + unidadDe(m) + ")";
    }

    // ------------------- Fase 2: justificacion (¿genera incidente?) -------------------

    private String justificacionDe(TipoMetrica m, double valor, Clasificacion c) {
        return switch (c.severidad()) {
            case informacion -> "La metrica volvio a un valor dentro del rango normal; se registra como "
                    + "informacion para el historial (monitoreo predictivo). No genera incidente.";
            case advertencia -> "Supera el umbral de advertencia (" + c.umbral() + " " + unidadDe(m) + ") pero el "
                    + "servicio no esta comprometido. Monitoreo proactivo: queda en observacion y NO genera "
                    + "incidente; si la tendencia continua, escalara a nivel alto.";
            case alto -> "Supera el umbral de nivel alto (" + c.umbral() + " " + unidadDe(m) + "): hay degradacion "
                    + "del servicio para los usuarios. SI genera incidente automaticamente para su diagnostico.";
            case critico -> "Supera el umbral critico (" + c.umbral() + " " + unidadDe(m) + "): riesgo inminente "
                    + "de indisponibilidad del Droplet y de los microservicios. SI genera incidente automaticamente "
                    + "con prioridad critica; puede escalarse a Jira (GDICJ) desde el panel.";
        };
    }

    // ------------------- Fase 5: plan de respuesta autodesignado -------------------

    private String accionDe(TipoMetrica m, SeveridadEvento s) {
        if (s == SeveridadEvento.informacion) return "Sin accion: registrar en el historial.";
        String porMetrica = switch (m) {
            case cpu -> "identificar el proceso/microservicio que consume CPU (top, docker stats)";
            case memoria -> "identificar fugas o contenedores con alto consumo de RAM (docker stats) y reiniciar el servicio afectado";
            case disco -> "depurar logs, imagenes Docker y archivos temporales; evaluar ampliar el volumen";
            case load -> "revisar la cola de procesos y los servicios con mas carga (uptime, htop)";
            case red_entrada -> "verificar origen del trafico entrante; descartar ataque o abuso de la API";
            case red_salida -> "verificar que servicio genera el trafico saliente; descartar exfiltracion o bucle de reintentos";
        };
        return switch (s) {
            case advertencia -> "Observar la tendencia y " + porMetrica + ".";
            case alto -> "Diagnosticar de inmediato: " + porMetrica + "; documentar en el incidente.";
            case critico -> "Intervencion inmediata: " + porMetrica + "; si no se estabiliza, escalar el Droplet "
                    + "(resize) o reiniciar servicios no criticos. Notificar a gerencia TI.";
            default -> "Sin accion.";
        };
    }

    private String responsableDe(SeveridadEvento s) {
        return switch (s) {
            case informacion -> "Monitoreo automatico";
            case advertencia -> "Soporte TI (turno)";
            case alto -> "Equipo de Infraestructura";
            case critico -> "Equipo de Infraestructura + Gerencia TI";
        };
    }

    private String tiempoMaximoDe(SeveridadEvento s) {
        return switch (s) {
            case informacion -> "-";
            case advertencia -> "60 min";
            case alto -> "15 min";
            case critico -> "5 min";
        };
    }

    private CategoriaIncidente categoriaDe(TipoMetrica m) {
        return switch (m) {
            case cpu, memoria, load -> CategoriaIncidente.rendimiento;
            case disco -> CategoriaIncidente.infraestructura;
            case red_entrada, red_salida -> CategoriaIncidente.redes;
        };
    }

    // ------------------- helpers -------------------

    private SeveridadEvento parseSeveridad(String s) {
        try { return SeveridadEvento.valueOf(s.toLowerCase()); }
        catch (Exception e) { throw new IllegalArgumentException("Severidad invalida: " + s); }
    }

    private EstadoEvento parseEstado(String s) {
        try { return EstadoEvento.valueOf(s.toLowerCase()); }
        catch (Exception e) { throw new IllegalArgumentException("Estado invalido: " + s); }
    }

    private TipoMetrica parseMetrica(String s) {
        try { return TipoMetrica.valueOf(s.toLowerCase()); }
        catch (Exception e) { throw new IllegalArgumentException("Metrica invalida: " + s); }
    }

    private String generarCodigo() {
        int year = LocalDate.now().getYear();
        String prefijo = "EVT-" + year + "-";
        int siguiente = repo.findByCodigoStartingWithOrderByCodigoDesc(prefijo).stream()
                .findFirst()
                .map(x -> {
                    String[] parts = x.getCodigo().split("-");
                    try { return Integer.parseInt(parts[2]) + 1; }
                    catch (Exception e) { return 1; }
                })
                .orElse(1);
        return prefijo + String.format("%03d", siguiente);
    }
}
