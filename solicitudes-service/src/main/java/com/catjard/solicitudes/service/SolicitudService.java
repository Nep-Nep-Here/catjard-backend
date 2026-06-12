package com.catjard.solicitudes.service;

import com.catjard.solicitudes.dto.ActualizarSolicitudDTO;
import com.catjard.solicitudes.dto.CrearSolicitudDTO;
import com.catjard.solicitudes.dto.SolicitudDTO;
import com.catjard.solicitudes.jira.JiraService;
import com.catjard.solicitudes.mapper.SolicitudMapper;
import com.catjard.solicitudes.model.EstadoSolicitud;
import com.catjard.solicitudes.model.Prioridad;
import com.catjard.solicitudes.model.Solicitud;
import com.catjard.solicitudes.model.TipoSolicitud;
import com.catjard.solicitudes.repository.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SolicitudService {

    private final SolicitudRepository repo;
    private final JiraService jira;

    @Transactional(readOnly = true)
    public List<SolicitudDTO> listar(Optional<String> estado, Optional<String> tipo) {
        List<Solicitud> data;
        if (estado.isPresent() && !estado.get().isBlank()) {
            data = repo.findByEstadoOrderByFechaDescIdDesc(parseEstado(estado.get()));
        } else if (tipo.isPresent() && !tipo.get().isBlank()) {
            data = repo.findByTipoOrderByFechaDescIdDesc(parseTipo(tipo.get()));
        } else {
            data = repo.findAllByOrderByFechaDescIdDesc();
        }
        return data.stream().map(SolicitudMapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<SolicitudDTO> listarMias(String email) {
        return repo.findBySolicitanteEmailIgnoreCaseOrderByIdDesc(email)
                .stream().map(SolicitudMapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public SolicitudDTO obtener(Long id) {
        return repo.findById(id).map(SolicitudMapper::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada: " + id));
    }

    // Crea la solicitud (estado 'nueva') y dispara la creacion del issue en Jira (best-effort).
    @Transactional
    public SolicitudDTO crear(CrearSolicitudDTO dto, String solicitanteEmail, String solicitanteNombre) {
        Solicitud s = Solicitud.builder()
                .codigo(generarCodigo())
                .fecha(LocalDate.now())
                .tipo(parseTipo(dto.tipo()))
                .asunto(dto.asunto())
                .descripcion(dto.descripcion())
                .prioridad(dto.prioridad() != null && !dto.prioridad().isBlank()
                        ? parsePrioridad(dto.prioridad()) : Prioridad.media)
                .estado(EstadoSolicitud.por_hacer)
                .solicitanteEmail(solicitanteEmail)
                .solicitanteNombre(solicitanteNombre)
                .build();

        Solicitud saved = repo.save(s);

        // Integracion Jira: best-effort. Si falla, la solicitud queda igual y se puede resincronizar.
        try {
            JiraService.JiraIssue issue = jira.crearIssue(saved);
            if (issue != null) {
                saved.setJiraIssueKey(issue.key());
                saved.setJiraUrl(issue.url());
            }
        } catch (Exception ex) {
            log.warn("No se pudo crear el issue en Jira para {}: {}", saved.getCodigo(), ex.getMessage());
        }

        return SolicitudMapper.toDTO(saved);
    }

    // El admin actualiza estado/prioridad/respuesta. Refleja el cambio como comentario en Jira.
    @Transactional
    public SolicitudDTO actualizar(Long id, ActualizarSolicitudDTO dto, String atendidoPor) {
        Solicitud s = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada: " + id));

        StringBuilder cambio = new StringBuilder();
        if (dto.estado() != null && !dto.estado().isBlank()) {
            s.setEstado(parseEstado(dto.estado()));
            cambio.append("Estado -> ").append(s.getEstado().name()).append(". ");
        }
        if (dto.prioridad() != null && !dto.prioridad().isBlank()) {
            s.setPrioridad(parsePrioridad(dto.prioridad()));
            cambio.append("Prioridad -> ").append(s.getPrioridad().name()).append(". ");
        }
        if (dto.respuesta() != null) {
            s.setRespuesta(dto.respuesta());
            cambio.append("Respuesta: ").append(dto.respuesta());
        }
        if (atendidoPor != null) s.setAtendidoPor(atendidoPor);

        // Reflejar en Jira (best-effort).
        try {
            if (s.getJiraIssueKey() != null && cambio.length() > 0) {
                jira.agregarComentario(s.getJiraIssueKey(),
                        "[" + (atendidoPor != null ? atendidoPor : "admin") + "] " + cambio);
            }
        } catch (Exception ex) {
            log.warn("No se pudo comentar en Jira el issue {}: {}",
                    s.getJiraIssueKey(), ex.getMessage());
        }

        return SolicitudMapper.toDTO(s);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!repo.existsById(id)) {
            throw new IllegalArgumentException("Solicitud no encontrada: " + id);
        }
        repo.deleteById(id);
    }

    // Sincroniza el estado de cada solicitud con el estado actual de su issue en Jira.
    // Jira es la fuente de verdad: mapea la categoria del estado a nuestros 3 estados.
    // Devuelve cuantas solicitudes cambiaron de estado.
    @Transactional
    public int sincronizarConJira() {
        if (!jira.isEnabled()) return 0;
        int actualizadas = 0;
        for (Solicitud s : repo.findAll()) {
            if (s.getJiraIssueKey() == null || s.getJiraIssueKey().isBlank()) continue;
            try {
                String categoria = jira.obtenerCategoriaEstado(s.getJiraIssueKey());
                if (categoria == null) continue;
                EstadoSolicitud nuevo = switch (categoria) {
                    case "new" -> EstadoSolicitud.por_hacer;
                    case "done" -> EstadoSolicitud.finalizado;
                    default -> EstadoSolicitud.en_curso; // "indeterminate" = en curso / en revision
                };
                if (s.getEstado() != nuevo) {
                    s.setEstado(nuevo);
                    actualizadas++;
                    log.info("Sync Jira: {} ({}) -> {}", s.getCodigo(), s.getJiraIssueKey(), nuevo);
                }
            } catch (Exception ex) {
                log.warn("Sync Jira fallo para {}: {}", s.getCodigo(), ex.getMessage());
            }
        }
        return actualizadas;
    }

    // ----------------- helpers -----------------

    private TipoSolicitud parseTipo(String s) {
        try { return TipoSolicitud.valueOf(s.toLowerCase()); }
        catch (Exception e) { throw new IllegalArgumentException("Tipo invalido: " + s); }
    }

    private EstadoSolicitud parseEstado(String s) {
        try { return EstadoSolicitud.valueOf(s.toLowerCase()); }
        catch (Exception e) { throw new IllegalArgumentException("Estado invalido: " + s); }
    }

    private Prioridad parsePrioridad(String s) {
        try { return Prioridad.valueOf(s.toLowerCase()); }
        catch (Exception e) { throw new IllegalArgumentException("Prioridad invalida: " + s); }
    }

    private String generarCodigo() {
        int year = LocalDate.now().getYear();
        String prefijo = "SOL-" + year + "-";
        int siguiente = repo.findByCodigoStartingWithOrderByCodigoDesc(prefijo).stream()
                .findFirst()
                .map(x -> {
                    String[] parts = x.getCodigo().split("-");
                    try { return Integer.parseInt(parts[2]) + 1; }
                    catch (Exception e) { return 1; }
                })
                .orElse(1);
        return prefijo + String.format("%04d", siguiente);
    }
}
