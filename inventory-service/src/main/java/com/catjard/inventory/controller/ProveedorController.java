package com.catjard.inventory.controller;

import com.catjard.inventory.dto.ActualizarProveedorDTO;
import com.catjard.inventory.dto.CrearProveedorDTO;
import com.catjard.inventory.dto.ProveedorDTO;
import com.catjard.inventory.service.ProveedorService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@Tag(name = "Proveedores", description = "Proveedores de insumos y productos")
@RestController
@RequestMapping("/api/proveedores")
@RequiredArgsConstructor
public class ProveedorController {

    private final ProveedorService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('almacen','gerente')")
    public List<ProveedorDTO> listar(@RequestParam Optional<Boolean> activos) {
        return service.listar(activos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('almacen','gerente')")
    public ProveedorDTO obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('almacen','gerente')")
    public ResponseEntity<ProveedorDTO> crear(@Valid @RequestBody CrearProveedorDTO dto) {
        ProveedorDTO creado = service.crear(dto);
        return ResponseEntity.created(URI.create("/api/proveedores/" + creado.id())).body(creado);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('almacen','gerente')")
    public ProveedorDTO actualizar(@PathVariable Long id, @Valid @RequestBody ActualizarProveedorDTO dto) {
        return service.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('gerente')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
