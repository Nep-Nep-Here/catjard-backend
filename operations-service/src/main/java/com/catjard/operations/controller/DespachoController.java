package com.catjard.operations.controller;

import com.catjard.operations.dto.CrearDespachoDTO;
import com.catjard.operations.dto.DespachoDTO;
import com.catjard.operations.dto.EntregarDTO;
import com.catjard.operations.service.DespachoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Tag(name = "Despachos", description = "Despachos de pedidos a courier")
@RestController
@RequestMapping("/api/despachos")
@RequiredArgsConstructor
public class DespachoController {

    private final DespachoService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('almacen','gerente','vendedor')")
    public List<DespachoDTO> listar() {
        return service.listar();
    }

    @GetMapping("/pedido/{codigo}")
    @PreAuthorize("hasAnyRole('almacen','gerente','vendedor','cliente')")
    public DespachoDTO obtenerPorPedido(@PathVariable String codigo) {
        return service.obtenerPorPedido(codigo);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('almacen','gerente')")
    public ResponseEntity<DespachoDTO> crear(@Valid @RequestBody CrearDespachoDTO dto) {
        DespachoDTO creado = service.crear(dto);
        return ResponseEntity.created(URI.create("/api/despachos/pedido/" + creado.pedidoCodigo())).body(creado);
    }

    @PostMapping("/pedido/{codigo}/entregar")
    @PreAuthorize("hasAnyRole('almacen','gerente')")
    public DespachoDTO entregar(@PathVariable String codigo, @RequestBody(required = false) EntregarDTO dto) {
        return service.marcarEntregado(codigo, dto);
    }
}
