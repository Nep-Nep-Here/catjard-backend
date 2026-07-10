package com.catjard.solicitudes.service;

import com.catjard.solicitudes.dto.ActualizarArticuloKBDTO;
import com.catjard.solicitudes.dto.ArticuloKBDTO;
import com.catjard.solicitudes.dto.CrearArticuloKBDTO;
import com.catjard.solicitudes.model.ArticuloKB;
import com.catjard.solicitudes.model.CategoriaIncidente;
import com.catjard.solicitudes.model.CategoriaKB;
import com.catjard.solicitudes.model.ServicioCritico;
import com.catjard.solicitudes.repository.ArticuloKBRepository;
import com.catjard.solicitudes.repository.ServicioCriticoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

// Base de Conocimiento del equipo TI: planes (continuidad, DRP, respaldos),
// politicas y runbooks con la estrategia de recuperacion por escenario.
// Las "sugerencias" conectan la KB con la Gestion de Incidentes: dado un
// incidente (categoria + servicio), devuelve las estrategias documentadas.
@Slf4j
@Service
@RequiredArgsConstructor
public class BaseConocimientoService {

    private final ArticuloKBRepository repo;
    private final ServicioCriticoRepository servicioRepo;

    @Transactional(readOnly = true)
    public List<ArticuloKBDTO> listar(Optional<String> categoria, Optional<String> q) {
        List<ArticuloKB> data = repo.findAllByOrderByFechaActualizacionDescIdDesc();
        if (categoria.isPresent() && !categoria.get().isBlank()) {
            CategoriaKB cat = parseCategoria(categoria.get());
            data = data.stream().filter(a -> a.getCategoria() == cat).toList();
        }
        if (q.isPresent() && !q.get().isBlank()) {
            String needle = normalizar(q.get());
            data = data.stream().filter(a ->
                    normalizar(a.getTitulo()).contains(needle)
                    || normalizar(a.getResumen()).contains(needle)
                    || normalizar(a.getContenido()).contains(needle)
                    || normalizar(a.getCodigo()).contains(needle)).toList();
        }
        Map<Long, String> nombres = nombresServicios();
        return data.stream().map(a -> toDTO(a, nombres)).toList();
    }

    // Abrir un articulo cuenta como lectura (contador de vistas del panel).
    @Transactional
    public ArticuloKBDTO obtener(Long id) {
        ArticuloKB a = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Articulo no encontrado: " + id));
        a.setVistas(a.getVistas() + 1);
        return toDTO(a, nombresServicios());
    }

    // Estrategias documentadas que aplican a un incidente: por categoria de
    // incidente y/o por servicio afectado (sin duplicar).
    @Transactional(readOnly = true)
    public List<ArticuloKBDTO> sugerencias(Optional<String> categoriaIncidente, Optional<Long> servicioId) {
        LinkedHashSet<ArticuloKB> out = new LinkedHashSet<>();
        if (categoriaIncidente.isPresent() && !categoriaIncidente.get().isBlank()) {
            try {
                out.addAll(repo.findByCategoriaIncidenteOrderByIdAsc(
                        CategoriaIncidente.valueOf(categoriaIncidente.get().toLowerCase())));
            } catch (IllegalArgumentException ignored) { }
        }
        servicioId.ifPresent(id -> out.addAll(repo.findByServicioIdOrderByIdAsc(id)));
        Map<Long, String> nombres = nombresServicios();
        return out.stream().map(a -> toDTO(a, nombres)).toList();
    }

    // Primera estrategia aplicable a un incidente, como referencia corta para Jira.
    @Transactional(readOnly = true)
    public Optional<String> referenciaParaJira(CategoriaIncidente categoria, Long servicioId) {
        List<ArticuloKB> porCategoria = categoria != null
                ? repo.findByCategoriaIncidenteOrderByIdAsc(categoria) : List.of();
        Optional<ArticuloKB> elegido = porCategoria.stream().findFirst();
        if (elegido.isEmpty() && servicioId != null) {
            elegido = repo.findByServicioIdOrderByIdAsc(servicioId).stream().findFirst();
        }
        return elegido.map(a -> a.getCodigo() + " — " + a.getTitulo());
    }

    @Transactional
    public ArticuloKBDTO crear(CrearArticuloKBDTO dto) {
        validarServicio(dto.servicioId());
        ArticuloKB a = ArticuloKB.builder()
                .codigo(generarCodigo())
                .titulo(dto.titulo())
                .categoria(parseCategoria(dto.categoria()))
                .resumen(dto.resumen())
                .contenido(dto.contenido())
                .autor(dto.autor() != null && !dto.autor().isBlank() ? dto.autor() : "Equipo TI Cat Jard")
                .vistas(0)
                .categoriaIncidente(parseCategoriaIncidente(dto.categoriaIncidente()))
                .servicioId(dto.servicioId())
                .build();
        return toDTO(repo.save(a), nombresServicios());
    }

    @Transactional
    public ArticuloKBDTO actualizar(Long id, ActualizarArticuloKBDTO dto) {
        ArticuloKB a = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Articulo no encontrado: " + id));
        if (dto.titulo() != null && !dto.titulo().isBlank()) a.setTitulo(dto.titulo());
        if (dto.categoria() != null && !dto.categoria().isBlank()) a.setCategoria(parseCategoria(dto.categoria()));
        if (dto.resumen() != null) a.setResumen(dto.resumen());
        if (dto.contenido() != null && !dto.contenido().isBlank()) a.setContenido(dto.contenido());
        if (dto.autor() != null) a.setAutor(dto.autor());
        if (dto.categoriaIncidente() != null) {
            a.setCategoriaIncidente(parseCategoriaIncidente(dto.categoriaIncidente()));
        }
        if (dto.servicioId() != null) {
            if (dto.servicioId() <= 0) a.setServicioId(null);   // 0 = desvincular
            else { validarServicio(dto.servicioId()); a.setServicioId(dto.servicioId()); }
        }
        return toDTO(a, nombresServicios());
    }

    @Transactional
    public void eliminar(Long id) {
        if (!repo.existsById(id)) throw new IllegalArgumentException("Articulo no encontrado: " + id);
        repo.deleteById(id);
    }

    // ----------------- helpers -----------------

    private ArticuloKBDTO toDTO(ArticuloKB a, Map<Long, String> nombresServicios) {
        return new ArticuloKBDTO(
                a.getId(), a.getCodigo(), a.getTitulo(), a.getCategoria().name(),
                a.getResumen(), a.getContenido(), a.getAutor(), a.getVistas(),
                a.getCategoriaIncidente() != null ? a.getCategoriaIncidente().name() : null,
                a.getServicioId(), nombresServicios.get(a.getServicioId()),
                a.getFechaCreacion(), a.getFechaActualizacion());
    }

    private Map<Long, String> nombresServicios() {
        return servicioRepo.findAll().stream()
                .collect(Collectors.toMap(ServicioCritico::getId, ServicioCritico::getNombre, (x, y) -> x));
    }

    private void validarServicio(Long servicioId) {
        if (servicioId != null && servicioId > 0 && !servicioRepo.existsById(servicioId)) {
            throw new IllegalArgumentException("Servicio no encontrado: " + servicioId);
        }
    }

    private CategoriaKB parseCategoria(String s) {
        try { return CategoriaKB.valueOf(s.toLowerCase()); }
        catch (Exception e) { throw new IllegalArgumentException("Categoria de articulo invalida: " + s); }
    }

    private CategoriaIncidente parseCategoriaIncidente(String s) {
        if (s == null || s.isBlank()) return null;
        try { return CategoriaIncidente.valueOf(s.toLowerCase()); }
        catch (Exception e) { throw new IllegalArgumentException("Categoria de incidente invalida: " + s); }
    }

    // Busqueda tolerante a tildes y mayusculas.
    private String normalizar(String s) {
        if (s == null) return "";
        return Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase();
    }

    private String generarCodigo() {
        int year = LocalDate.now().getYear();
        String prefijo = "KB-" + year + "-";
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
