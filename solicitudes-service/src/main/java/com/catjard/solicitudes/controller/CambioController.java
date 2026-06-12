package com.catjard.solicitudes.controller;

import com.catjard.solicitudes.dto.ActualizarCambioDTO;
import com.catjard.solicitudes.dto.CambioDTO;
import com.catjard.solicitudes.dto.CrearCambioDTO;
import com.catjard.solicitudes.service.CambioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "Control de Cambios", description = "Plan de control de cambios + flujo de aprobacion + integracion continua (Jira)")
@RestController
@RequestMapping("/api/cambios")
@RequiredArgsConstructor
public class CambioController {

    private final CambioService service;

    // Personal interno solicita un cambio (Responsable TIC / area).
    @PostMapping
    @PreAuthorize("hasAnyRole('vendedor','almacen','produccion','gerente')")
    public ResponseEntity<CambioDTO> crear(@Valid @RequestBody CrearCambioDTO dto, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto, auth.getName()));
    }

    // Tablero / lista (todos los internos pueden ver el estado).
    @GetMapping
    @PreAuthorize("hasAnyRole('vendedor','almacen','produccion','gerente')")
    public List<CambioDTO> listar(@RequestParam Optional<String> estado) {
        return service.listar(estado);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('vendedor','almacen','produccion','gerente')")
    public CambioDTO obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    // Sincroniza ahora los estados con el tablero del equipo de sistemas en Jira.
    @PostMapping("/sync")
    @PreAuthorize("hasAnyRole('vendedor','almacen','produccion','gerente')")
    public java.util.Map<String, Integer> sincronizar() {
        return java.util.Map.of("actualizadas", service.sincronizarConJira());
    }

    // Evaluar / aprobar / rechazar y avanzar la CI: exclusivo de gerencia (CAB).
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('gerente')")
    public CambioDTO actualizar(@PathVariable Long id,
                                @Valid @RequestBody ActualizarCambioDTO dto,
                                Authentication auth) {
        return service.actualizar(id, dto, auth.getName());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('gerente')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
