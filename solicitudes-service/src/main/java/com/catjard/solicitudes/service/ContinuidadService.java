package com.catjard.solicitudes.service;

import com.catjard.solicitudes.digitalocean.DigitalOceanService;
import com.catjard.solicitudes.dto.*;
import com.catjard.solicitudes.mapper.ContinuidadMapper;
import com.catjard.solicitudes.model.*;
import com.catjard.solicitudes.repository.IncidenteRepository;
import com.catjard.solicitudes.repository.RespaldoRepository;
import com.catjard.solicitudes.repository.RiesgoRepository;
import com.catjard.solicitudes.repository.ServicioCriticoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

// Gestion de Continuidad del Servicio y Recuperacion ante Desastres (DRP).
// Une las fases del plan sobre datos vivos del sistema:
//   Fase 1/3: catalogo de servicios criticos con RTO/RPO objetivo.
//   Fase 2:   matriz de riesgos (Probabilidad x Impacto) por servicio.
//   Fase 5:   registro de respaldos (regla 3-2-1) -> semaforo RPO por servicio.
//   Resumen:  cumplimiento RTO medido sobre incidentes + estado RPO (la tabla
//             de "resultados esperados" del plan, calculada en vivo).
@Slf4j
@Service
@RequiredArgsConstructor
public class ContinuidadService {

    private final ServicioCriticoRepository servicioRepo;
    private final RiesgoRepository riesgoRepo;
    private final RespaldoRepository respaldoRepo;
    private final IncidenteRepository incidenteRepo;
    private final DigitalOceanService digitalOcean;

    // ------------------- Fase 1/3: catalogo de servicios criticos -------------------

    @Transactional(readOnly = true)
    public List<ServicioCriticoDTO> listarServicios() {
        return servicioRepo.findAllByOrderByPrioridadRecuperacionAscIdAsc()
                .stream().map(ContinuidadMapper::toDTO).toList();
    }

    @Transactional
    public ServicioCriticoDTO crearServicio(CrearServicioCriticoDTO dto) {
        ServicioCritico s = ServicioCritico.builder()
                .codigo(generarCodigoServicio())
                .nombre(dto.nombre())
                .descripcion(dto.descripcion())
                .tipo(parseTipoServicio(dto.tipo()))
                .criticidad(parseCriticidad(dto.criticidad()))
                .prioridadRecuperacion(dto.prioridadRecuperacion())
                .rtoMinutos(dto.rtoMinutos())
                .rpoMinutos(dto.rpoMinutos())
                .estrategiaContinuidad(dto.estrategiaContinuidad())
                .activo(true)
                .build();
        return ContinuidadMapper.toDTO(servicioRepo.save(s));
    }

    @Transactional
    public ServicioCriticoDTO actualizarServicio(Long id, ActualizarServicioCriticoDTO dto) {
        ServicioCritico s = servicioRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado: " + id));
        if (dto.nombre() != null && !dto.nombre().isBlank()) s.setNombre(dto.nombre());
        if (dto.descripcion() != null) s.setDescripcion(dto.descripcion());
        if (dto.tipo() != null && !dto.tipo().isBlank()) s.setTipo(parseTipoServicio(dto.tipo()));
        if (dto.criticidad() != null && !dto.criticidad().isBlank()) s.setCriticidad(parseCriticidad(dto.criticidad()));
        if (dto.prioridadRecuperacion() != null) s.setPrioridadRecuperacion(dto.prioridadRecuperacion());
        if (dto.rtoMinutos() != null) s.setRtoMinutos(dto.rtoMinutos());
        // El RPO se aplica tal cual venga (incluido null): el formulario de edicion envia
        // el payload completo, asi que dejar el campo vacio DEBE quitarle el objetivo RPO al
        // servicio (p.ej. un microservicio sin datos propios) y sacarlo del semaforo.
        s.setRpoMinutos(dto.rpoMinutos());
        if (dto.estrategiaContinuidad() != null) s.setEstrategiaContinuidad(dto.estrategiaContinuidad());
        if (dto.activo() != null) s.setActivo(dto.activo());
        return ContinuidadMapper.toDTO(s);
    }

    @Transactional
    public void eliminarServicio(Long id) {
        if (!servicioRepo.existsById(id)) throw new IllegalArgumentException("Servicio no encontrado: " + id);
        servicioRepo.deleteById(id);
    }

    // ------------------- Fase 2: matriz de riesgos -------------------

    @Transactional(readOnly = true)
    public List<RiesgoDTO> listarRiesgos() {
        return riesgoRepo.findAllByOrderByIdAsc().stream()
                .sorted(Comparator.comparing((Riesgo r) -> r.getNivelRiesgo().ordinal()).reversed()
                        .thenComparing(Riesgo::getId))
                .map(ContinuidadMapper::toDTO)
                .toList();
    }

    @Transactional
    public RiesgoDTO crearRiesgo(CrearRiesgoDTO dto) {
        Nivel probabilidad = parseNivel(dto.probabilidad());
        Nivel impacto = parseNivel(dto.impacto());
        Riesgo r = Riesgo.builder()
                .codigo(generarCodigoRiesgo())
                .nombre(dto.nombre())
                .descripcion(dto.descripcion())
                .probabilidad(probabilidad)
                .impacto(impacto)
                .nivelRiesgo(calcularNivelRiesgo(probabilidad, impacto))
                .accionMitigacion(dto.accionMitigacion())
                .estado(EstadoRiesgo.identificado)
                .servicios(resolverServicios(dto.servicioIds()))
                .build();
        return ContinuidadMapper.toDTO(riesgoRepo.save(r));
    }

    @Transactional
    public RiesgoDTO actualizarRiesgo(Long id, ActualizarRiesgoDTO dto) {
        Riesgo r = riesgoRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Riesgo no encontrado: " + id));
        if (dto.nombre() != null && !dto.nombre().isBlank()) r.setNombre(dto.nombre());
        if (dto.descripcion() != null) r.setDescripcion(dto.descripcion());
        boolean recalcular = false;
        if (dto.probabilidad() != null && !dto.probabilidad().isBlank()) { r.setProbabilidad(parseNivel(dto.probabilidad())); recalcular = true; }
        if (dto.impacto() != null && !dto.impacto().isBlank()) { r.setImpacto(parseNivel(dto.impacto())); recalcular = true; }
        if (recalcular) r.setNivelRiesgo(calcularNivelRiesgo(r.getProbabilidad(), r.getImpacto()));
        if (dto.accionMitigacion() != null) r.setAccionMitigacion(dto.accionMitigacion());
        if (dto.estado() != null && !dto.estado().isBlank()) r.setEstado(parseEstadoRiesgo(dto.estado()));
        if (dto.servicioIds() != null) r.setServicios(resolverServicios(dto.servicioIds()));
        return ContinuidadMapper.toDTO(r);
    }

    @Transactional
    public void eliminarRiesgo(Long id) {
        if (!riesgoRepo.existsById(id)) throw new IllegalArgumentException("Riesgo no encontrado: " + id);
        riesgoRepo.deleteById(id);
    }

    // Misma matriz de pesos que la priorizacion de incidentes (ITIL):
    // bajo=1, medio=2, alto=3; el producto define el nivel.
    private NivelRiesgo calcularNivelRiesgo(Nivel probabilidad, Nivel impacto) {
        int score = peso(probabilidad) * peso(impacto);
        if (score >= 9) return NivelRiesgo.critico;
        if (score >= 5) return NivelRiesgo.alto;
        if (score >= 3) return NivelRiesgo.medio;
        return NivelRiesgo.bajo;
    }
    private int peso(Nivel n) {
        return switch (n) { case bajo -> 1; case medio -> 2; case alto -> 3; };
    }

    private Set<ServicioCritico> resolverServicios(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return new LinkedHashSet<>();
        List<ServicioCritico> encontrados = servicioRepo.findAllById(ids);
        if (encontrados.size() != new HashSet<>(ids).size()) {
            throw new IllegalArgumentException("Algun servicio de la lista no existe: " + ids);
        }
        return new LinkedHashSet<>(encontrados);
    }

    // ------------------- Fase 5: registro de respaldos -------------------

    @Transactional(readOnly = true)
    public List<RespaldoDTO> listarRespaldos() {
        Map<Long, String> nombres = nombresServicios();
        return respaldoRepo.findTop100ByOrderByFechaHoraDescIdDesc().stream()
                .map(r -> ContinuidadMapper.toDTO(r, nombres.get(r.getServicioId())))
                .toList();
    }

    @Transactional
    public RespaldoDTO registrarRespaldo(RegistrarRespaldoDTO dto, OrigenRespaldo origen) {
        if (dto.servicioId() != null && !servicioRepo.existsById(dto.servicioId())) {
            throw new IllegalArgumentException("Servicio no encontrado: " + dto.servicioId());
        }
        Respaldo r = Respaldo.builder()
                .codigo(generarCodigoRespaldo())
                .fechaHora(dto.fechaHora() != null ? dto.fechaHora() : LocalDateTime.now())
                .servicioId(dto.servicioId())
                .recurso(dto.recurso())
                .tipo(parseTipoRespaldo(dto.tipo()))
                .destino(parseDestino(dto.destino()))
                .estado(parseEstadoRespaldo(dto.estado()))
                .tamanoMb(dto.tamanoMb())
                .duracionSeg(dto.duracionSeg())
                .mensaje(dto.mensaje())
                .origen(origen)
                .build();
        Respaldo saved = respaldoRepo.save(r);
        log.info("Respaldo {} registrado: {} [{} -> {}] {}", saved.getCodigo(), saved.getRecurso(),
                saved.getTipo(), saved.getDestino(), saved.getEstado());
        return ContinuidadMapper.toDTO(saved, nombresServicios().get(saved.getServicioId()));
    }

    // Sincroniza los backups automaticos y snapshots del Droplet desde la API de
    // DigitalOcean (mismo token read-only del monitoreo). Dedupe por externo_id:
    // se puede llamar cuantas veces se quiera. Devuelve cuantos se registraron.
    @Transactional
    public int sincronizarRespaldosDO() {
        if (!digitalOcean.isEnabled()) return 0;
        DigitalOceanService.Droplet droplet = digitalOcean.obtenerDroplet();
        if (droplet == null) {
            log.warn("Sync respaldos DO: no se encontro ningun droplet en la cuenta.");
            return 0;
        }

        // Las imagenes del Droplet se cuelgan del servicio de infraestructura del catalogo.
        Long servicioId = servicioRepo
                .findFirstByTipoAndActivoTrueOrderByIdAsc(TipoServicio.infraestructura)
                .map(ServicioCritico::getId).orElse(null);

        int creados = 0;
        for (DigitalOceanService.ImagenRespaldo img : digitalOcean.respaldosDroplet(droplet.id())) {
            if (respaldoRepo.existsByExternoId(img.id())) continue;
            Respaldo r = Respaldo.builder()
                    .codigo(generarCodigoRespaldo())
                    .fechaHora(LocalDateTime.ofInstant(img.creado(), ZoneId.systemDefault()))
                    .servicioId(servicioId)
                    .recurso("Droplet completo — " + img.nombre())
                    .tipo(TipoRespaldo.snapshot)
                    .destino(DestinoRespaldo.snapshot_do)
                    .estado(EstadoRespaldo.exitoso)
                    .tamanoMb(img.sizeGb() != null ? img.sizeGb() * 1024 : null)
                    .mensaje(img.automatico()
                            ? "Backup automatico del plan de Backups de DigitalOcean."
                            : "Snapshot tomado desde el panel de DigitalOcean.")
                    .origen(OrigenRespaldo.digitalocean)
                    .externoId(img.id())
                    .build();
            respaldoRepo.save(r);
            creados++;
            log.info("Sync respaldos DO: {} registrado ({}).", r.getCodigo(), img.nombre());
        }
        return creados;
    }

    // ------------------- Resumen: cumplimiento RTO/RPO en vivo -------------------

    @Transactional(readOnly = true)
    public ResumenContinuidadDTO resumen() {
        List<ServicioCritico> servicios = servicioRepo.findAllByOrderByPrioridadRecuperacionAscIdAsc();

        long criticidadAlta = servicios.stream()
                .filter(s -> s.getCriticidad() == CriticidadServicio.alta || s.getCriticidad() == CriticidadServicio.critica)
                .count();
        long riesgosAbiertos = riesgoRepo.countByEstadoIn(
                List.of(EstadoRiesgo.identificado, EstadoRiesgo.en_mitigacion));

        // Cumplimiento RTO: incidentes que tuvieron deadline y ya fueron medidos.
        List<Incidente> conRto = incidenteRepo.findByRtoDeadlineIsNotNull();
        long cumplidos = conRto.stream().filter(i -> Boolean.TRUE.equals(i.getCumplioRto())).count();
        long incumplidos = conRto.stream().filter(i -> Boolean.FALSE.equals(i.getCumplioRto())).count();
        long medidos = cumplidos + incumplidos;
        Double porcentaje = medidos > 0 ? Math.round(cumplidos * 1000.0 / medidos) / 10.0 : null;

        LocalDateTime ahora = LocalDateTime.now();
        long activosVencidos = conRto.stream()
                // Los cancelados son falsos positivos: su contador esta apagado y no
                // hay servicio que recuperar, asi que no son un vencido "activo".
                .filter(i -> i.getEstado() != EstadoIncidente.cancelado)
                .filter(i -> i.getCumplioRto() == null && i.getFechaResolucion() == null
                        && i.getRtoDeadline().isBefore(ahora))
                .count();

        // Semaforo RPO: solo servicios con datos propios (RPO objetivo definido).
        List<ResumenContinuidadDTO.EstadoRpoDTO> estadoRpo = servicios.stream()
                .filter(s -> s.isActivo() && s.getRpoMinutos() != null)
                .map(s -> {
                    Optional<Respaldo> ultimo = respaldoRepo
                            .findTopByServicioIdAndEstadoOrderByFechaHoraDesc(s.getId(), EstadoRespaldo.exitoso);
                    LocalDateTime fecha = ultimo.map(Respaldo::getFechaHora).orElse(null);
                    Long minutos = fecha != null ? Duration.between(fecha, ahora).toMinutes() : null;
                    boolean cumple = minutos != null && minutos <= s.getRpoMinutos();
                    return new ResumenContinuidadDTO.EstadoRpoDTO(
                            s.getId(), s.getNombre(), s.getRpoMinutos(), fecha, minutos, cumple);
                })
                .toList();

        return new ResumenContinuidadDTO(
                servicios.size(), criticidadAlta, riesgosAbiertos,
                conRto.size(), cumplidos, incumplidos, porcentaje, activosVencidos, estadoRpo);
    }

    // ------------------- helpers -------------------

    private Map<Long, String> nombresServicios() {
        return servicioRepo.findAll().stream()
                .collect(Collectors.toMap(ServicioCritico::getId, ServicioCritico::getNombre, (a, b) -> a));
    }

    private TipoServicio parseTipoServicio(String s) {
        try { return TipoServicio.valueOf(s.toLowerCase()); }
        catch (Exception e) { throw new IllegalArgumentException("Tipo de servicio invalido: " + s); }
    }
    private CriticidadServicio parseCriticidad(String s) {
        try { return CriticidadServicio.valueOf(s.toLowerCase()); }
        catch (Exception e) { throw new IllegalArgumentException("Criticidad invalida: " + s); }
    }
    private Nivel parseNivel(String s) {
        try { return Nivel.valueOf(s.toLowerCase()); }
        catch (Exception e) { throw new IllegalArgumentException("Nivel invalido: " + s); }
    }
    private EstadoRiesgo parseEstadoRiesgo(String s) {
        try { return EstadoRiesgo.valueOf(s.toLowerCase()); }
        catch (Exception e) { throw new IllegalArgumentException("Estado de riesgo invalido: " + s); }
    }
    private TipoRespaldo parseTipoRespaldo(String s) {
        try { return TipoRespaldo.valueOf(s.toLowerCase()); }
        catch (Exception e) { throw new IllegalArgumentException("Tipo de respaldo invalido: " + s); }
    }
    private DestinoRespaldo parseDestino(String s) {
        try { return DestinoRespaldo.valueOf(s.toLowerCase()); }
        catch (Exception e) { throw new IllegalArgumentException("Destino invalido: " + s); }
    }
    private EstadoRespaldo parseEstadoRespaldo(String s) {
        try { return EstadoRespaldo.valueOf(s.toLowerCase()); }
        catch (Exception e) { throw new IllegalArgumentException("Estado de respaldo invalido: " + s); }
    }

    // Los servicios usan correlativo simple (catalogo estable, sin anio).
    private String generarCodigoServicio() {
        String prefijo = "SRV-";
        int siguiente = servicioRepo.findByCodigoStartingWithOrderByCodigoDesc(prefijo).stream()
                .findFirst()
                .map(x -> {
                    try { return Integer.parseInt(x.getCodigo().substring(prefijo.length())) + 1; }
                    catch (Exception e) { return 1; }
                })
                .orElse(1);
        return prefijo + String.format("%03d", siguiente);
    }

    private String generarCodigoRiesgo() {
        int year = LocalDate.now().getYear();
        String prefijo = "RSG-" + year + "-";
        return prefijo + String.format("%03d", siguientePorCodigo(
                riesgoRepo.findByCodigoStartingWithOrderByCodigoDesc(prefijo).stream()
                        .findFirst().map(Riesgo::getCodigo)));
    }

    private String generarCodigoRespaldo() {
        int year = LocalDate.now().getYear();
        String prefijo = "RSP-" + year + "-";
        return prefijo + String.format("%03d", siguientePorCodigo(
                respaldoRepo.findByCodigoStartingWithOrderByCodigoDesc(prefijo).stream()
                        .findFirst().map(Respaldo::getCodigo)));
    }

    private int siguientePorCodigo(Optional<String> ultimoCodigo) {
        return ultimoCodigo.map(c -> {
            String[] parts = c.split("-");
            try { return Integer.parseInt(parts[2]) + 1; }
            catch (Exception e) { return 1; }
        }).orElse(1);
    }
}
