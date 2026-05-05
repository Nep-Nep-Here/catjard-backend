package com.catjard.operations.controller;

import com.catjard.operations.dto.ArteDTO;
import com.catjard.operations.dto.RevisionArteDTO;
import com.catjard.operations.dto.SubirArteDTO;
import com.catjard.operations.service.ArteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Tag(name = "Artes", description = "Versiones de arte por pedido")
@RestController
@RequestMapping("/api/artes")
@RequiredArgsConstructor
public class ArteController {

    private final ArteService service;

    @GetMapping("/pedido/{codigo}")
    @PreAuthorize("hasAnyRole('produccion','gerente','vendedor','cliente')")
    public List<ArteDTO> listarPorPedido(@PathVariable String codigo) {
        return service.listarPorPedido(codigo);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('produccion','gerente','vendedor','cliente')")
    public ArteDTO obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping("/pedido/{codigo}")
    @PreAuthorize("hasAnyRole('produccion','gerente')")
    public ResponseEntity<ArteDTO> subir(@PathVariable String codigo, @Valid @RequestBody SubirArteDTO dto) {
        ArteDTO creado = service.subirNuevaVersion(codigo, dto);
        return ResponseEntity.created(URI.create("/api/artes/" + creado.id())).body(creado);
    }

    @PostMapping("/{id}/aprobar")
    @PreAuthorize("hasAnyRole('cliente','vendedor','gerente')")
    public ArteDTO aprobar(@PathVariable Long id, @RequestBody(required = false) RevisionArteDTO dto) {
        return service.aprobar(id, dto);
    }

    @PostMapping("/{id}/rechazar")
    @PreAuthorize("hasAnyRole('cliente','vendedor','gerente')")
    public ArteDTO rechazar(@PathVariable Long id, @RequestBody(required = false) RevisionArteDTO dto) {
        return service.rechazar(id, dto);
    }
}
