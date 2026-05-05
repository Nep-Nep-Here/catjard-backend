package com.catjard.inventory.controller;

import com.catjard.inventory.dto.CrearOrdenCompraDTO;
import com.catjard.inventory.dto.OrdenCompraDTO;
import com.catjard.inventory.service.OrdenCompraService;
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

@Tag(name = "OrdenesCompra", description = "Ordenes de compra a proveedores")
@RestController
@RequestMapping("/api/ordenes-compra")
@RequiredArgsConstructor
public class OrdenCompraController {

    private final OrdenCompraService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('almacen','gerente')")
    public List<OrdenCompraDTO> listar(@RequestParam Optional<String> estado,
                                       @RequestParam Optional<Long> proveedorId) {
        return service.listar(estado, proveedorId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('almacen','gerente')")
    public OrdenCompraDTO obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @GetMapping("/codigo/{codigo}")
    @PreAuthorize("hasAnyRole('almacen','gerente')")
    public OrdenCompraDTO obtenerPorCodigo(@PathVariable String codigo) {
        return service.obtenerPorCodigo(codigo);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('almacen','gerente')")
    public ResponseEntity<OrdenCompraDTO> crear(@Valid @RequestBody CrearOrdenCompraDTO dto) {
        OrdenCompraDTO creada = service.crear(dto);
        return ResponseEntity.created(URI.create("/api/ordenes-compra/" + creada.id())).body(creada);
    }

    @PostMapping("/{id}/enviar")
    @PreAuthorize("hasAnyRole('almacen','gerente')")
    public OrdenCompraDTO enviar(@PathVariable Long id) {
        return service.enviar(id);
    }

    @PostMapping("/{id}/recibir")
    @PreAuthorize("hasAnyRole('almacen','gerente')")
    public OrdenCompraDTO recibir(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String usuario = body != null ? body.get("usuario") : null;
        return service.recibir(id, usuario);
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('almacen','gerente')")
    public OrdenCompraDTO cancelar(@PathVariable Long id) {
        return service.cancelar(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('gerente')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
