package com.catjard.solicitudes.controller;

import com.catjard.solicitudes.dto.*;
import com.catjard.solicitudes.model.OrigenRespaldo;
import com.catjard.solicitudes.service.ContinuidadService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Gestion de Continuidad del Servicio y DRP",
     description = "Catalogo de servicios criticos (RTO/RPO), matriz de riesgos, "
             + "registro de respaldos (regla 3-2-1) y resumen de cumplimiento en vivo")
@RestController
@RequestMapping("/api/continuidad")
@RequiredArgsConstructor
public class ContinuidadController {

    private final ContinuidadService service;

    // Token compartido con el cron de respaldos del Droplet (no es un JWT de usuario).
    @Value("${continuidad.backup-token:}")
    private String backupToken;

    // ----- resumen (tablero del plan) -----

    @GetMapping("/resumen")
    @PreAuthorize("hasAnyRole('vendedor','almacen','produccion','gerente')")
    public ResumenContinuidadDTO resumen() {
        return service.resumen();
    }

    // ----- Fase 1/3: catalogo de servicios criticos -----

    @GetMapping("/servicios")
    @PreAuthorize("hasAnyRole('vendedor','almacen','produccion','gerente')")
    public List<ServicioCriticoDTO> listarServicios() {
        return service.listarServicios();
    }

    @PostMapping("/servicios")
    @PreAuthorize("hasRole('gerente')")
    public ResponseEntity<ServicioCriticoDTO> crearServicio(@Valid @RequestBody CrearServicioCriticoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearServicio(dto));
    }

    @PatchMapping("/servicios/{id}")
    @PreAuthorize("hasRole('gerente')")
    public ServicioCriticoDTO actualizarServicio(@PathVariable Long id,
                                                 @Valid @RequestBody ActualizarServicioCriticoDTO dto) {
        return service.actualizarServicio(id, dto);
    }

    @DeleteMapping("/servicios/{id}")
    @PreAuthorize("hasRole('gerente')")
    public ResponseEntity<Void> eliminarServicio(@PathVariable Long id) {
        service.eliminarServicio(id);
        return ResponseEntity.noContent().build();
    }

    // ----- Fase 2: matriz de riesgos -----

    @GetMapping("/riesgos")
    @PreAuthorize("hasAnyRole('vendedor','almacen','produccion','gerente')")
    public List<RiesgoDTO> listarRiesgos() {
        return service.listarRiesgos();
    }

    @PostMapping("/riesgos")
    @PreAuthorize("hasRole('gerente')")
    public ResponseEntity<RiesgoDTO> crearRiesgo(@Valid @RequestBody CrearRiesgoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearRiesgo(dto));
    }

    @PatchMapping("/riesgos/{id}")
    @PreAuthorize("hasRole('gerente')")
    public RiesgoDTO actualizarRiesgo(@PathVariable Long id, @Valid @RequestBody ActualizarRiesgoDTO dto) {
        return service.actualizarRiesgo(id, dto);
    }

    @DeleteMapping("/riesgos/{id}")
    @PreAuthorize("hasRole('gerente')")
    public ResponseEntity<Void> eliminarRiesgo(@PathVariable Long id) {
        service.eliminarRiesgo(id);
        return ResponseEntity.noContent().build();
    }

    // ----- Fase 5: registro de respaldos -----

    @GetMapping("/respaldos")
    @PreAuthorize("hasAnyRole('vendedor','almacen','produccion','gerente')")
    public List<RespaldoDTO> listarRespaldos() {
        return service.listarRespaldos();
    }

    // Sincroniza los backups automaticos y snapshots del Droplet desde la API de
    // DigitalOcean (mismo token del monitoreo). Idempotente: dedupe por imagen.
    @PostMapping("/respaldos/sync-do")
    @PreAuthorize("hasAnyRole('vendedor','almacen','produccion','gerente')")
    public java.util.Map<String, Integer> sincronizarRespaldosDO() {
        return java.util.Map.of("creados", service.sincronizarRespaldosDO());
    }

    // Registro manual desde el panel de gerencia (p.ej. un snapshot tomado a mano).
    @PostMapping("/respaldos")
    @PreAuthorize("hasRole('gerente')")
    public ResponseEntity<RespaldoDTO> registrarRespaldo(@Valid @RequestBody RegistrarRespaldoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.registrarRespaldo(dto, OrigenRespaldo.manual));
    }

    // Registro automatico del cron de respaldos del Droplet: autentica con el
    // token compartido X-Backup-Token (definido en CONTINUIDAD_BACKUP_TOKEN),
    // no con JWT, porque el script no tiene usuario. Trazabilidad del plan 3-2-1.
    @PostMapping("/respaldos/script")
    public ResponseEntity<RespaldoDTO> registrarRespaldoScript(
            @RequestHeader(name = "X-Backup-Token", required = false) String token,
            @Valid @RequestBody RegistrarRespaldoDTO dto) {
        if (backupToken == null || backupToken.isBlank() || !backupToken.equals(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.registrarRespaldo(dto, OrigenRespaldo.script));
    }
}
