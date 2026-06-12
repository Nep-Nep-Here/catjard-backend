package com.catjard.solicitudes.controller;

import com.catjard.solicitudes.dto.ActualizarSolicitudDTO;
import com.catjard.solicitudes.dto.CrearSolicitudDTO;
import com.catjard.solicitudes.dto.SolicitudDTO;
import com.catjard.solicitudes.service.SolicitudService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Tag(name = "Solicitudes", description = "Mesa de ayuda / tickets (servicio, informacion, acceso) con integracion Jira")
@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
public class SolicitudController {

    private final SolicitudService service;

    // Abrir una solicitud desde la pagina de Ayuda.
    // - Con login (cliente, vendedor, almacen, produccion): el email sale del JWT.
    // - Sin login (formulario publico de la web): el email/nombre vienen en el body.
    @PostMapping
    public ResponseEntity<SolicitudDTO> crear(@Valid @RequestBody CrearSolicitudDTO dto, Authentication auth) {
        boolean autenticado = auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getName());
        String email = autenticado ? auth.getName() : dto.solicitanteEmail();
        String nombre = autenticado ? auth.getName() : dto.solicitanteNombre();
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Indica un correo para poder darte seguimiento.");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto, email, nombre));
    }

    // El usuario ve sus propias solicitudes.
    @GetMapping("/mias")
    @PreAuthorize("hasAnyRole('cliente','vendedor','almacen','produccion','gerente')")
    public List<SolicitudDTO> mias(Authentication auth) {
        return service.listarMias(auth.getName());
    }

    // El admin (gerente) ve todas (panel de administracion).
    @GetMapping
    @PreAuthorize("hasRole('gerente')")
    public List<SolicitudDTO> listar(@RequestParam Optional<String> estado,
                                     @RequestParam Optional<String> tipo) {
        return service.listar(estado, tipo);
    }

    // Sincroniza ahora los estados con Jira (lo usa el boton "Actualizar" del panel).
    @PostMapping("/sync")
    @PreAuthorize("hasAnyRole('vendedor','almacen','produccion','gerente')")
    public java.util.Map<String, Integer> sincronizar() {
        return java.util.Map.of("actualizadas", service.sincronizarConJira());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('gerente')")
    public SolicitudDTO obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    // El admin avanza el estado, ajusta prioridad y responde. Se refleja en Jira.
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('gerente')")
    public SolicitudDTO actualizar(@PathVariable Long id,
                                   @Valid @RequestBody ActualizarSolicitudDTO dto,
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
