package com.catjard.inventory.controller;

import com.catjard.inventory.dto.CrearMovimientoDTO;
import com.catjard.inventory.dto.MovimientoDTO;
import com.catjard.inventory.service.MovimientoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@Tag(name = "Movimientos", description = "Movimientos de inventario (entradas/salidas/ajustes)")
@RestController
@RequestMapping("/api/movimientos")
@RequiredArgsConstructor
public class MovimientoController {

    private final MovimientoService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('almacen','gerente','produccion')")
    public List<MovimientoDTO> listar(@RequestParam Optional<String> tipo,
                                      @RequestParam Optional<Long> productoId,
                                      @RequestParam Optional<String> referencia) {
        return service.listar(tipo, productoId, referencia);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('almacen','gerente','produccion')")
    public ResponseEntity<MovimientoDTO> crear(@Valid @RequestBody CrearMovimientoDTO dto) {
        MovimientoDTO creado = service.crear(dto);
        return ResponseEntity.created(URI.create("/api/movimientos/" + creado.id())).body(creado);
    }
}
