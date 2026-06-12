package com.catjard.solicitudes.service;

import com.catjard.solicitudes.dto.ActualizarCambioDTO;
import com.catjard.solicitudes.dto.CambioDTO;
import com.catjard.solicitudes.dto.CrearCambioDTO;
import com.catjard.solicitudes.jira.JiraService;
import com.catjard.solicitudes.mapper.CambioMapper;
import com.catjard.solicitudes.model.*;
import com.catjard.solicitudes.repository.CambioRepository;
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
public class CambioService {

    private final CambioRepository repo;
    private final JiraService jira;

    @Transactional(readOnly = true)
    public List<CambioDTO> listar(Optional<String> estado) {
        List<Cambio> data = (estado.isPresent() && !estado.get().isBlank())
                ? repo.findByEstadoOrderByFechaDescIdDesc(parseEstado(estado.get()))
                : repo.findAllByOrderByFechaDescIdDesc();
        return data.stream().map(CambioMapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public CambioDTO obtener(Long id) {
        return repo.findById(id).map(CambioMapper::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Cambio no encontrado: " + id));
    }

    // Solicitar un cambio: crea el registro (estado 'solicitado') y abre el issue en Jira.
    @Transactional
    public CambioDTO crear(CrearCambioDTO dto, String solicitanteEmail) {
        TipoCambio tipo = parseTipo(dto.tipoCambio());
        Cambio c = Cambio.builder()
                .codigo(generarCodigo(tipo))
                .fecha(LocalDate.now())
                .proyecto(dto.proyecto())
                .version("1.0")
                .tipoCambio(tipo)
                .categoria(parseCategoria(dto.categoria()))
                .titulo(dto.titulo())
                .descripcion(dto.descripcion())
                .impacto(dto.impacto() != null && !dto.impacto().isBlank() ? parseNivel(dto.impacto()) : Nivel.medio)
                .prioridad(dto.prioridad() != null && !dto.prioridad().isBlank() ? parsePrioridad(dto.prioridad()) : Prioridad.media)
                .estado(EstadoCambio.solicitado)
                .etapaCI(EtapaCI.pendiente)
                .responsable(dto.responsable())
                .solicitanteEmail(solicitanteEmail)
                .areaAfectada(dto.areaAfectada())
                .riesgo(dto.riesgo())
                .nivelRiesgo(dto.nivelRiesgo() != null && !dto.nivelRiesgo().isBlank() ? parseNivel(dto.nivelRiesgo()) : null)
                .planPruebas(dto.planPruebas())
                .planRollback(dto.planRollback())
                .build();

        Cambio saved = repo.save(c);

        try {
            JiraService.JiraIssue issue = jira.crearIssue(saved);
            if (issue != null) {
                saved.setJiraIssueKey(issue.key());
                saved.setJiraUrl(issue.url());
            }
        } catch (Exception ex) {
            log.warn("No se pudo crear el issue de cambio {} en Jira: {}", saved.getCodigo(), ex.getMessage());
        }

        return CambioMapper.toDTO(saved);
    }

    // Evaluar / aprobar / rechazar / mover el flujo y la etapa de CI.
    @Transactional
    public CambioDTO actualizar(Long id, ActualizarCambioDTO dto, String quien) {
        Cambio c = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cambio no encontrado: " + id));

        StringBuilder cambio = new StringBuilder();

        if (dto.estado() != null && !dto.estado().isBlank()) {
            EstadoCambio nuevo = parseEstado(dto.estado());
            c.setEstado(nuevo);
            cambio.append("Estado -> ").append(nuevo.name()).append(". ");
            // Al aprobar, el pipeline CI queda listo para iniciar.
            if (nuevo == EstadoCambio.aprobado && c.getEtapaCI() == EtapaCI.pendiente) {
                c.setEtapaCI(EtapaCI.repositorio);
                cambio.append("CI iniciada (repositorio). ");
            }
            if (nuevo == EstadoCambio.rechazado) {
                c.setEtapaCI(EtapaCI.pendiente);
            }
        }
        if (dto.etapaCI() != null && !dto.etapaCI().isBlank()) {
            if (c.getEstado() != EstadoCambio.aprobado && c.getEstado() != EstadoCambio.implementado
                    && c.getEstado() != EstadoCambio.en_monitoreo) {
                throw new IllegalStateException("La Integracion Continua solo avanza con el cambio aprobado.");
            }
            EtapaCI etapa = parseEtapa(dto.etapaCI());
            c.setEtapaCI(etapa);
            cambio.append("CI -> ").append(etapa.name()).append(". ");
        }
        if (dto.prioridad() != null && !dto.prioridad().isBlank()) c.setPrioridad(parsePrioridad(dto.prioridad()));
        if (dto.impacto() != null && !dto.impacto().isBlank()) c.setImpacto(parseNivel(dto.impacto()));
        if (dto.nivelRiesgo() != null && !dto.nivelRiesgo().isBlank()) c.setNivelRiesgo(parseNivel(dto.nivelRiesgo()));
        if (dto.responsable() != null) c.setResponsable(dto.responsable());
        if (dto.areaAfectada() != null) c.setAreaAfectada(dto.areaAfectada());
        if (dto.riesgo() != null) c.setRiesgo(dto.riesgo());
        if (dto.planPruebas() != null) c.setPlanPruebas(dto.planPruebas());
        if (dto.planRollback() != null) c.setPlanRollback(dto.planRollback());

        try {
            if (c.getJiraIssueKey() != null && cambio.length() > 0) {
                jira.agregarComentario(c.getJiraIssueKey(), "[" + (quien != null ? quien : "admin") + "] " + cambio);
            }
        } catch (Exception ex) {
            log.warn("No se pudo comentar en Jira el cambio {}: {}", c.getCodigo(), ex.getMessage());
        }

        return CambioMapper.toDTO(c);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!repo.existsById(id)) throw new IllegalArgumentException("Cambio no encontrado: " + id);
        repo.deleteById(id);
    }

    // Sincroniza el estado de cada cambio con su issue en el proyecto Jira del equipo
    // de sistemas (CDCCJ). Jira es la fuente de verdad: mapea por NOMBRE de estado
    // (las columnas del tablero replican el flujo) y deriva la etapa de CI.
    // Si el nombre no se reconoce, no toca nada (conservador).
    @Transactional
    public int sincronizarConJira() {
        if (!jira.isEnabled()) return 0;
        int actualizadas = 0;
        for (Cambio c : repo.findAll()) {
            if (c.getJiraIssueKey() == null || c.getJiraIssueKey().isBlank()) continue;
            try {
                JiraService.JiraEstado je = jira.obtenerEstado(c.getJiraIssueKey());
                if (je == null || je.nombre() == null) continue;
                EstadoCambio nuevo = mapearEstadoJira(je.nombre());
                if (nuevo == null) continue;
                EtapaCI etapa = derivarEtapaCI(nuevo);
                if (c.getEstado() != nuevo || c.getEtapaCI() != etapa) {
                    c.setEstado(nuevo);
                    c.setEtapaCI(etapa);
                    actualizadas++;
                    log.info("Sync Jira: {} ({}) -> {} / CI {}", c.getCodigo(), c.getJiraIssueKey(), nuevo, etapa);
                }
            } catch (Exception ex) {
                log.warn("Sync Jira fallo para {}: {}", c.getCodigo(), ex.getMessage());
            }
        }
        return actualizadas;
    }

    // Nombre del estado en Jira -> estado del flujo (sin tildes, case-insensitive).
    private EstadoCambio mapearEstadoJira(String nombre) {
        String n = java.text.Normalizer.normalize(nombre, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase().trim();
        return switch (n) {
            case "solicitado", "por hacer" -> EstadoCambio.solicitado;
            case "en evaluacion", "en revision" -> EstadoCambio.en_evaluacion;
            case "aprobado" -> EstadoCambio.aprobado;
            case "en implementacion", "implementado", "en curso" -> EstadoCambio.implementado;
            case "en monitoreo" -> EstadoCambio.en_monitoreo;
            case "cerrado", "finalizado", "listo" -> EstadoCambio.cerrado;
            case "rechazado" -> EstadoCambio.rechazado;
            default -> null;
        };
    }

    // La etapa del pipeline CI se deriva del estado del flujo (visualizacion automatica).
    private EtapaCI derivarEtapaCI(EstadoCambio estado) {
        return switch (estado) {
            case solicitado, en_evaluacion, rechazado -> EtapaCI.pendiente;
            case aprobado -> EtapaCI.repositorio;
            case implementado -> EtapaCI.testing;
            case en_monitoreo -> EtapaCI.monitoreo;
            case cerrado -> EtapaCI.completado;
        };
    }

    // ----------------- helpers -----------------

    private TipoCambio parseTipo(String s) {
        try { return TipoCambio.valueOf(s.toLowerCase()); }
        catch (Exception e) { throw new IllegalArgumentException("Tipo de cambio invalido: " + s); }
    }
    private CategoriaCambio parseCategoria(String s) {
        try { return CategoriaCambio.valueOf(s.toLowerCase()); }
        catch (Exception e) { throw new IllegalArgumentException("Categoria invalida: " + s); }
    }
    private EstadoCambio parseEstado(String s) {
        try { return EstadoCambio.valueOf(s.toLowerCase()); }
        catch (Exception e) { throw new IllegalArgumentException("Estado invalido: " + s); }
    }
    private EtapaCI parseEtapa(String s) {
        try { return EtapaCI.valueOf(s.toLowerCase()); }
        catch (Exception e) { throw new IllegalArgumentException("Etapa CI invalida: " + s); }
    }
    private Nivel parseNivel(String s) {
        try { return Nivel.valueOf(s.toLowerCase()); }
        catch (Exception e) { throw new IllegalArgumentException("Nivel invalido: " + s); }
    }
    private Prioridad parsePrioridad(String s) {
        try { return Prioridad.valueOf(s.toLowerCase()); }
        catch (Exception e) { throw new IllegalArgumentException("Prioridad invalida: " + s); }
    }

    private String prefijo(TipoCambio t) {
        return switch (t) {
            case estandar -> "CHE";
            case normal -> "CHN";
            case emergencia -> "CHM";
        };
    }

    private String generarCodigo(TipoCambio tipo) {
        int year = LocalDate.now().getYear();
        String prefijo = prefijo(tipo) + "-" + year + "-";
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
