package com.catjard.solicitudes.controller;

import com.catjard.solicitudes.dto.ActualizarIncidenteDTO;
import com.catjard.solicitudes.dto.CrearIncidenteDTO;
import com.catjard.solicitudes.dto.IncidenteDTO;
import com.catjard.solicitudes.service.IncidenteService;
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

@Tag(name = "Gestion de Incidentes",
     description = "Registro, clasificacion, priorizacion (matriz Impacto x Urgencia), diagnostico y cierre de incidentes (Jira)")
@RestController
@RequestMapping("/api/incidentes")
@RequiredArgsConstructor
public class IncidenteController {

    private final IncidenteService service;

    // Personal interno registra un incidente (usuario / monitoreo / mesa de ayuda).
    @PostMapping
    @PreAuthorize("hasAnyRole('vendedor','almacen','produccion','gerente')")
    public ResponseEntity<IncidenteDTO> crear(@Valid @RequestBody CrearIncidenteDTO dto, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto, auth.getName()));
    }

    // Tablero / lista de incidentes.
    @GetMapping
    @PreAuthorize("hasAnyRole('vendedor','almacen','produccion','gerente')")
    public List<IncidenteDTO> listar(@RequestParam Optional<String> estado) {
        return service.listar(estado);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('vendedor','almacen','produccion','gerente')")
    public IncidenteDTO obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    // Sincroniza ahora los estados con el tablero GDICJ en Jira.
    @PostMapping("/sync")
    @PreAuthorize("hasAnyRole('vendedor','almacen','produccion','gerente')")
    public java.util.Map<String, Integer> sincronizar() {
        return java.util.Map.of("actualizadas", service.sincronizarConJira());
    }

    // Boton "Enviar a Jira": abre el issue en GDICJ para un incidente que aun no lo tiene.
    @PostMapping("/{id}/enviar-jira")
    @PreAuthorize("hasRole('gerente')")
    public IncidenteDTO enviarAJira(@PathVariable Long id) {
        return service.enviarAJira(id);
    }

    // Diagnosticar / resolver / cerrar y avanzar el flujo: gerencia (Mesa de Ayuda / Sistemas).
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('gerente')")
    public IncidenteDTO actualizar(@PathVariable Long id,
                                   @Valid @RequestBody ActualizarIncidenteDTO dto,
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
