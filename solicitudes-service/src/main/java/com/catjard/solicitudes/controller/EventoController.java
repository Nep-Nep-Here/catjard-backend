package com.catjard.solicitudes.controller;

import com.catjard.solicitudes.dto.ActualizarEventoDTO;
import com.catjard.solicitudes.dto.EventoDTO;
import com.catjard.solicitudes.dto.MetricaActualDTO;
import com.catjard.solicitudes.dto.SimularEventoDTO;
import com.catjard.solicitudes.service.EventoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Tag(name = "Monitoreo Estrategico y Gestion de Eventos",
     description = "Eventos del Droplet (API DigitalOcean): clasificacion por severidad, "
             + "auto-generacion de incidentes (error/critical) y escalamiento opcional a Jira")
@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
public class EventoController {

    private final EventoService service;

    // Panel de eventos: lista con filtros por severidad o estado.
    @GetMapping
    @PreAuthorize("hasAnyRole('vendedor','almacen','produccion','gerente')")
    public List<EventoDTO> listar(@RequestParam Optional<String> severidad,
                                  @RequestParam Optional<String> estado) {
        return service.listar(severidad, estado);
    }

    // Lecturas en vivo de las metricas del Droplet (gauges del panel).
    @GetMapping("/metricas")
    @PreAuthorize("hasAnyRole('vendedor','almacen','produccion','gerente')")
    public List<MetricaActualDTO> metricas() {
        return service.metricasActuales();
    }

    // Politicas de alerta configuradas en el panel de DigitalOcean.
    @GetMapping("/alertas-do")
    @PreAuthorize("hasAnyRole('vendedor','almacen','produccion','gerente')")
    public List<Map<String, Object>> alertasDigitalOcean() {
        return service.alertasDigitalOcean();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('vendedor','almacen','produccion','gerente')")
    public EventoDTO obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    // Fuerza una evaluacion de metricas ahora (sin esperar al scheduler).
    @PostMapping("/sync")
    @PreAuthorize("hasAnyRole('vendedor','almacen','produccion','gerente')")
    public Map<String, Integer> sincronizar() {
        return Map.of("creados", service.evaluarMetricas());
    }

    // Simula una lectura (demo): pasa por el mismo pipeline que las lecturas reales.
    @PostMapping("/simular")
    @PreAuthorize("hasAnyRole('vendedor','almacen','produccion','gerente')")
    public ResponseEntity<EventoDTO> simular(@Valid @RequestBody SimularEventoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.simular(dto));
    }

    // Boton "Enviar a Jira": escala el incidente vinculado al tablero GDICJ (criticos).
    @PostMapping("/{id}/enviar-jira")
    @PreAuthorize("hasAnyRole('vendedor','almacen','produccion','gerente')")
    public EventoDTO enviarAJira(@PathVariable Long id) {
        return service.enviarAJira(id);
    }

    // Gestionar el evento en el panel: estado / responsable / plan de respuesta.
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('gerente')")
    public EventoDTO actualizar(@PathVariable Long id, @Valid @RequestBody ActualizarEventoDTO dto) {
        return service.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('gerente')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
