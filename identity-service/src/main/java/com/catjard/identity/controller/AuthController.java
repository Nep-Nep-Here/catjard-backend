package com.catjard.identity.controller;

import com.catjard.identity.dto.*;
import com.catjard.identity.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "Login, registro publico y datos del usuario actual")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;

    // MECANISMO DE SEGURIDAD: validación de entrada con @Valid (Jakarta Bean Validation).
    // Las anotaciones @NotBlank/@Email/@Size en LoginRequest se aplican antes de entrar
    // al servicio. Si el payload no cumple, Spring devuelve 400 Bad Request automáticamente.
    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest req) {
        return service.login(req);
    }

    // MECANISMO DE SEGURIDAD: validación de entrada con @Valid (ver RegistroDTO).
    // Garantiza email válido, password mínimo 6 caracteres y RUC de 11 dígitos antes
    // de tocar la base de datos.
    @PostMapping("/registro")
    public ResponseEntity<UsuarioDTO> registro(@RequestBody @Valid RegistroDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(dto));
    }

    @GetMapping("/me")
    public UsuarioDTO me(Authentication auth) {
        return service.me(auth.getName());
    }
}
