package com.catjard.identity.controller;

import com.catjard.identity.dto.ActualizarUsuarioDTO;
import com.catjard.identity.dto.CrearUsuarioDTO;
import com.catjard.identity.dto.UsuarioDTO;
import com.catjard.identity.service.UsuarioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Usuarios", description = "CRUD de usuarios (solo gerente)")
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('gerente')")
public class UsuarioController {

    private final UsuarioService service;

    @GetMapping
    public List<UsuarioDTO> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public UsuarioDTO obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    public ResponseEntity<UsuarioDTO> crear(@RequestBody @Valid CrearUsuarioDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    @PatchMapping("/{id}")
    public UsuarioDTO actualizar(@PathVariable Long id,
                                 @RequestBody ActualizarUsuarioDTO dto) {
        return service.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
