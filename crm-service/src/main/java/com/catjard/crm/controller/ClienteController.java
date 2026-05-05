package com.catjard.crm.controller;

import com.catjard.crm.dto.ActualizarClienteDTO;
import com.catjard.crm.dto.ClienteDTO;
import com.catjard.crm.dto.CrearClienteDTO;
import com.catjard.crm.service.ClienteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@Tag(name = "Clientes", description = "Clientes B2B del CRM")
@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('vendedor','gerente','almacen')")
    public List<ClienteDTO> listar(@RequestParam Optional<Boolean> activos) {
        return service.listar(activos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('vendedor','gerente','almacen')")
    public ClienteDTO obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @GetMapping("/ruc/{ruc}")
    @PreAuthorize("hasAnyRole('vendedor','gerente','almacen')")
    public ClienteDTO obtenerPorRuc(@PathVariable String ruc) {
        return service.obtenerPorRuc(ruc);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('vendedor','gerente')")
    public ResponseEntity<ClienteDTO> crear(@Valid @RequestBody CrearClienteDTO dto) {
        ClienteDTO creado = service.crear(dto);
        return ResponseEntity.created(URI.create("/api/clientes/" + creado.id())).body(creado);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('vendedor','gerente')")
    public ClienteDTO actualizar(@PathVariable Long id, @Valid @RequestBody ActualizarClienteDTO dto) {
        return service.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('gerente')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
