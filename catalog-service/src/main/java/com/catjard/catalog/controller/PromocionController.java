package com.catjard.catalog.controller;

import com.catjard.catalog.dto.ActualizarPromocionDTO;
import com.catjard.catalog.dto.CrearPromocionDTO;
import com.catjard.catalog.dto.PromocionDTO;
import com.catjard.catalog.service.PromocionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Promociones", description = "Promociones del catalogo (todo / categoria / producto)")
@RestController
@RequestMapping("/api/promociones")
@RequiredArgsConstructor
public class PromocionController {

    private final PromocionService service;

    @GetMapping
    public List<PromocionDTO> listar() {
        return service.listar();
    }

    @GetMapping("/activas")
    public List<PromocionDTO> activas() {
        return service.listarActivas();
    }

    @GetMapping("/{id}")
    public PromocionDTO obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('vendedor','gerente')")
    public ResponseEntity<PromocionDTO> crear(@RequestBody @Valid CrearPromocionDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('vendedor','gerente')")
    public PromocionDTO actualizar(@PathVariable Long id, @RequestBody ActualizarPromocionDTO dto) {
        return service.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('vendedor','gerente')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
