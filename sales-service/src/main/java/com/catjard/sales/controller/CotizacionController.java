package com.catjard.sales.controller;

import com.catjard.sales.dto.ActualizarCotizacionDTO;
import com.catjard.sales.dto.CotizacionDTO;
import com.catjard.sales.dto.CrearCotizacionDTO;
import com.catjard.sales.service.CotizacionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Tag(name = "Cotizaciones", description = "Cotizaciones B2B")
@RestController
@RequestMapping("/api/cotizaciones")
@RequiredArgsConstructor
public class CotizacionController {

    private final CotizacionService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('vendedor','gerente','cliente')")
    public List<CotizacionDTO> listar(@RequestParam Optional<String> estado,
                                      @RequestParam Optional<Long> clienteId) {
        return service.listar(estado, clienteId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('vendedor','gerente','cliente')")
    public CotizacionDTO obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @GetMapping("/codigo/{codigo}")
    @PreAuthorize("hasAnyRole('vendedor','gerente','cliente')")
    public CotizacionDTO obtenerPorCodigo(@PathVariable String codigo) {
        return service.obtenerPorCodigo(codigo);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('vendedor','gerente','cliente')")
    public ResponseEntity<CotizacionDTO> crear(@Valid @RequestBody CrearCotizacionDTO dto) {
        CotizacionDTO creado = service.crear(dto);
        return ResponseEntity.created(URI.create("/api/cotizaciones/" + creado.id())).body(creado);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('vendedor','gerente')")
    public CotizacionDTO actualizar(@PathVariable Long id, @Valid @RequestBody ActualizarCotizacionDTO dto) {
        return service.actualizar(id, dto);
    }

    @PostMapping("/{id}/aprobar")
    @PreAuthorize("hasAnyRole('vendedor','gerente','cliente')")
    public CotizacionDTO aprobar(@PathVariable Long id) {
        return service.aprobar(id);
    }

    @PostMapping("/{id}/rechazar")
    @PreAuthorize("hasAnyRole('vendedor','gerente','cliente')")
    public CotizacionDTO rechazar(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return service.rechazar(id, body.getOrDefault("motivo", ""));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('gerente')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
