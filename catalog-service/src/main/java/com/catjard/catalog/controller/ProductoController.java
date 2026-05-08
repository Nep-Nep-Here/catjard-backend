package com.catjard.catalog.controller;

import com.catjard.catalog.dto.ActualizarProductoDTO;
import com.catjard.catalog.dto.ActualizarStockDTO;
import com.catjard.catalog.dto.CrearProductoDTO;
import com.catjard.catalog.dto.ProductoDTO;
import com.catjard.catalog.model.Categoria;
import com.catjard.catalog.service.ProductoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Productos", description = "Catalogo de productos (publico para consultar, restringido para CRUD)")
@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService service;

    @GetMapping
    public List<ProductoDTO> listar(@RequestParam(required = false) Categoria categoria,
                                    @RequestParam(required = false) String q) {
        return service.listar(categoria, q);
    }

    @GetMapping("/{id}")
    public ProductoDTO obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @GetMapping("/slug/{slug}")
    public ProductoDTO obtenerPorSlug(@PathVariable String slug) {
        return service.obtenerPorSlug(slug);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('vendedor','almacen','gerente')")
    public ResponseEntity<ProductoDTO> crear(@RequestBody @Valid CrearProductoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('vendedor','almacen','gerente')")
    public ProductoDTO actualizar(@PathVariable Long id, @RequestBody ActualizarProductoDTO dto) {
        return service.actualizar(id, dto);
    }

    @RequestMapping(value = "/{id}/stock", method = { RequestMethod.PATCH, RequestMethod.POST })
    @PreAuthorize("hasAnyRole('almacen','gerente')")
    public ProductoDTO actualizarStock(@PathVariable Long id, @RequestBody @Valid ActualizarStockDTO dto) {
        return service.actualizarStock(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('vendedor','almacen','gerente')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
