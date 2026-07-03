package com.catjard.solicitudes.service;

import com.catjard.solicitudes.dto.ActualizarIncidenteDTO;
import com.catjard.solicitudes.dto.CrearIncidenteDTO;
import com.catjard.solicitudes.dto.IncidenteDTO;
import com.catjard.solicitudes.jira.JiraService;
import com.catjard.solicitudes.mapper.IncidenteMapper;
import com.catjard.solicitudes.model.*;
import com.catjard.solicitudes.repository.IncidenteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IncidenteService {

    private final IncidenteRepository repo;
    private final JiraService jira;

    @Transactional(readOnly = true)
    public List<IncidenteDTO> listar(Optional<String> estado) {
        List<Incidente> data = (estado.isPresent() && !estado.get().isBlank())
                ? repo.findByEstadoOrderByFechaDescIdDesc(parseEstado(estado.get()))
                : repo.findAllByOrderByFechaDescIdDesc();
        return data.stream().map(IncidenteMapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public IncidenteDTO obtener(Long id) {
        return repo.findById(id).map(IncidenteMapper::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Incidente no encontrado: " + id));
    }

    // Registrar un incidente: crea el ticket (estado 'registrado'), calcula la prioridad
    // por la matriz Impacto x Urgencia y abre el issue en el tablero de Jira (GDICJ).
    @Transactional
    public IncidenteDTO crear(CrearIncidenteDTO dto, String solicitanteEmail) {
        Nivel impacto = parseNivel(dto.impacto());
        Nivel urgencia = parseNivel(dto.urgencia());
        Incidente i = Incidente.builder()
                .codigo(generarCodigo())
                .fecha(LocalDate.now())
                .titulo(dto.titulo())
                .descripcion(dto.descripcion())
                .origen(parseOrigen(dto.origen()))
                .servicioAfectado(dto.servicioAfectado())
                .categoria(parseCategoria(dto.categoria()))
                .impacto(impacto)
                .urgencia(urgencia)
                .prioridad(calcularPrioridad(impacto, urgencia))
                .estado(EstadoIncidente.registrado)
                .responsable(dto.responsable())
                .solicitanteEmail(solicitanteEmail)
                .diagnostico(dto.diagnostico())
                .solucion(dto.solucion())
                .evidencia(dto.evidencia())
                .build();

        Incidente saved = repo.save(i);

        try {
            JiraService.JiraIssue issue = jira.crearIssue(saved);
            if (issue != null) {
                saved.setJiraIssueKey(issue.key());
                saved.setJiraUrl(issue.url());
            }
        } catch (Exception ex) {
            log.warn("No se pudo crear el issue del incidente {} en Jira: {}", saved.getCodigo(), ex.getMessage());
        }

        return IncidenteMapper.toDTO(saved);
    }

    // Fase 2 (Identificacion de Eventos): un evento error/critical del monitoreo se convierte
    // automaticamente en incidente. NO abre issue en Jira: eso lo decide el operador con el
    // boton "Enviar a Jira" del panel de eventos (solo para los criticos).
    @Transactional
    public Incidente crearDesdeMonitoreo(String titulo, String descripcion, CategoriaIncidente categoria,
                                         Nivel impacto, Nivel urgencia) {
        Incidente i = Incidente.builder()
                .codigo(generarCodigo())
                .fecha(LocalDate.now())
                .titulo(titulo)
                .descripcion(descripcion)
                .origen(OrigenIncidente.monitoreo)
                .servicioAfectado("Droplet DigitalOcean (infraestructura Cat Jard)")
                .categoria(categoria)
                .impacto(impacto)
                .urgencia(urgencia)
                .prioridad(calcularPrioridad(impacto, urgencia))
                .estado(EstadoIncidente.registrado)
                .solicitanteEmail("monitoreo@catjard.local")
                .build();
        return repo.save(i);
    }

    // Gestionar: avanzar el flujo, reclasificar y registrar diagnostico/solucion.
    @Transactional
    public IncidenteDTO actualizar(Long id, ActualizarIncidenteDTO dto, String quien) {
        Incidente i = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Incidente no encontrado: " + id));

        StringBuilder cambio = new StringBuilder();

        if (dto.estado() != null && !dto.estado().isBlank()) {
            EstadoIncidente nuevo = parseEstado(dto.estado());
            aplicarEstado(i, nuevo);
            cambio.append("Estado -> ").append(nuevo.name()).append(". ");
        }

        boolean recalcular = false;
        if (dto.impacto() != null && !dto.impacto().isBlank()) { i.setImpacto(parseNivel(dto.impacto())); recalcular = true; }
        if (dto.urgencia() != null && !dto.urgencia().isBlank()) { i.setUrgencia(parseNivel(dto.urgencia())); recalcular = true; }
        if (recalcular) {
            i.setPrioridad(calcularPrioridad(i.getImpacto(), i.getUrgencia()));
            cambio.append("Prioridad -> ").append(i.getPrioridad().name()).append(". ");
        }

        if (dto.categoria() != null && !dto.categoria().isBlank()) i.setCategoria(parseCategoria(dto.categoria()));
        if (dto.responsable() != null) i.setResponsable(dto.responsable());
        if (dto.servicioAfectado() != null) i.setServicioAfectado(dto.servicioAfectado());
        if (dto.diagnostico() != null) i.setDiagnostico(dto.diagnostico());
        if (dto.solucion() != null) i.setSolucion(dto.solucion());
        if (dto.evidencia() != null) i.setEvidencia(dto.evidencia());

        try {
            if (i.getJiraIssueKey() != null && cambio.length() > 0) {
                jira.agregarComentario(i.getJiraIssueKey(), "[" + (quien != null ? quien : "soporte") + "] " + cambio);
            }
        } catch (Exception ex) {
            log.warn("No se pudo comentar en Jira el incidente {}: {}", i.getCodigo(), ex.getMessage());
        }

        return IncidenteMapper.toDTO(i);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!repo.existsById(id)) throw new IllegalArgumentException("Incidente no encontrado: " + id);
        repo.deleteById(id);
    }

    // Sincroniza el estado de cada incidente con su issue en el tablero GDICJ.
    // Jira es la fuente de verdad: mapea por NOMBRE de columna (case-insensitive, sin tildes).
    @Transactional
    public int sincronizarConJira() {
        if (!jira.isEnabled()) return 0;
        int actualizadas = 0;
        for (Incidente i : repo.findAll()) {
            if (i.getJiraIssueKey() == null || i.getJiraIssueKey().isBlank()) continue;
            try {
                JiraService.JiraEstado je = jira.obtenerEstado(i.getJiraIssueKey());
                if (je == null || je.nombre() == null) continue;
                EstadoIncidente nuevo = mapearEstadoJira(je.nombre());
                if (nuevo == null || nuevo == i.getEstado()) continue;
                aplicarEstado(i, nuevo);
                actualizadas++;
                log.info("Sync Jira: incidente {} ({}) -> {}", i.getCodigo(), i.getJiraIssueKey(), nuevo);
            } catch (Exception ex) {
                log.warn("Sync Jira fallo para {}: {}", i.getCodigo(), ex.getMessage());
            }
        }
        return actualizadas;
    }

    // Cambia el estado y marca las fechas de resolucion/cierre cuando corresponde.
    private void aplicarEstado(Incidente i, EstadoIncidente nuevo) {
        i.setEstado(nuevo);
        if (nuevo == EstadoIncidente.resuelto && i.getFechaResolucion() == null) {
            i.setFechaResolucion(LocalDateTime.now());
        }
        if (nuevo == EstadoIncidente.cerrado) {
            if (i.getFechaResolucion() == null) i.setFechaResolucion(LocalDateTime.now());
            i.setFechaCierre(LocalDateTime.now());
        }
        if (nuevo == EstadoIncidente.reabierto) {
            i.setFechaResolucion(null);
            i.setFechaCierre(null);
        }
    }

    // Matriz de priorizacion ITIL: Prioridad = f(Impacto, Urgencia).
    // Se mapea cada nivel a un peso (bajo=1, medio=2, alto=3) y se cruza el producto:
    //   alto x alta (9) = critica
    //   alto x media / medio x alta (6) = alta
    //   alto x baja / medio x media / bajo x alta (3-4) = media
    //   medio x baja / bajo x media / bajo x baja (1-2) = baja
    private PrioridadIncidente calcularPrioridad(Nivel impacto, Nivel urgencia) {
        int score = peso(impacto) * peso(urgencia);
        if (score >= 9) return PrioridadIncidente.critica;
        if (score >= 5) return PrioridadIncidente.alta;
        if (score >= 3) return PrioridadIncidente.media;
        return PrioridadIncidente.baja;
    }
    private int peso(Nivel n) {
        return switch (n) { case bajo -> 1; case medio -> 2; case alto -> 3; };
    }

    // Nombre del estado en Jira -> estado del flujo (sin tildes, case-insensitive).
    // Si el nombre no se reconoce, devuelve null (conservador: no toca el incidente).
    private EstadoIncidente mapearEstadoJira(String nombre) {
        String n = java.text.Normalizer.normalize(nombre, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase().trim();
        return switch (n) {
            case "registrado", "nuevo", "por hacer", "to do", "abierto" -> EstadoIncidente.registrado;
            case "en diagnostico", "diagnostico", "en analisis", "analisis" -> EstadoIncidente.en_diagnostico;
            case "en resolucion", "resolucion", "en proceso", "en curso", "in progress" -> EstadoIncidente.en_resolucion;
            case "resuelto", "en validacion", "validacion" -> EstadoIncidente.resuelto;
            case "cerrado", "finalizado", "listo", "done" -> EstadoIncidente.cerrado;
            case "reabierto" -> EstadoIncidente.reabierto;
            case "cancelado", "descartado" -> EstadoIncidente.cancelado;
            default -> null;
        };
    }

    // ----------------- helpers -----------------

    private EstadoIncidente parseEstado(String s) {
        try { return EstadoIncidente.valueOf(s.toLowerCase()); }
        catch (Exception e) { throw new IllegalArgumentException("Estado invalido: " + s); }
    }
    private CategoriaIncidente parseCategoria(String s) {
        try { return CategoriaIncidente.valueOf(s.toLowerCase()); }
        catch (Exception e) { throw new IllegalArgumentException("Categoria invalida: " + s); }
    }
    private OrigenIncidente parseOrigen(String s) {
        try { return OrigenIncidente.valueOf(s.toLowerCase()); }
        catch (Exception e) { throw new IllegalArgumentException("Origen invalido: " + s); }
    }
    private Nivel parseNivel(String s) {
        try { return Nivel.valueOf(s.toLowerCase()); }
        catch (Exception e) { throw new IllegalArgumentException("Nivel invalido: " + s); }
    }

    private String generarCodigo() {
        int year = LocalDate.now().getYear();
        String prefijo = "INC-" + year + "-";
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
