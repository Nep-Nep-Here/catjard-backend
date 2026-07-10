package com.catjard.solicitudes.controller;

import com.catjard.solicitudes.dto.ActualizarArticuloKBDTO;
import com.catjard.solicitudes.dto.ArticuloKBDTO;
import com.catjard.solicitudes.dto.CrearArticuloKBDTO;
import com.catjard.solicitudes.service.BaseConocimientoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "Base de Conocimiento",
     description = "Planes de continuidad, DRP, respaldos, politicas y runbooks del equipo TI. "
             + "Todos los roles leen; gerencia edita. Las sugerencias vinculan articulos con incidentes.")
@RestController
@RequestMapping("/api/continuidad/kb")
@RequiredArgsConstructor
public class BaseConocimientoController {

    private final BaseConocimientoService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('vendedor','almacen','produccion','gerente')")
    public List<ArticuloKBDTO> listar(@RequestParam Optional<String> categoria,
                                      @RequestParam Optional<String> q) {
        return service.listar(categoria, q);
    }

    // Estrategias documentadas que aplican a un incidente (categoria y/o servicio).
    @GetMapping("/sugerencias")
    @PreAuthorize("hasAnyRole('vendedor','almacen','produccion','gerente')")
    public List<ArticuloKBDTO> sugerencias(@RequestParam Optional<String> categoriaIncidente,
                                           @RequestParam Optional<Long> servicioId) {
        return service.sugerencias(categoriaIncidente, servicioId);
    }

    // Abrir un articulo suma una vista (metricas de uso de la KB).
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('vendedor','almacen','produccion','gerente')")
    public ArticuloKBDTO obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('gerente')")
    public ResponseEntity<ArticuloKBDTO> crear(@Valid @RequestBody CrearArticuloKBDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('gerente')")
    public ArticuloKBDTO actualizar(@PathVariable Long id, @Valid @RequestBody ActualizarArticuloKBDTO dto) {
        return service.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('gerente')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
