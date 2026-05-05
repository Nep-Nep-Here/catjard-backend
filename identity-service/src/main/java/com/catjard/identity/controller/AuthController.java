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

    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest req) {
        return service.login(req);
    }

    @PostMapping("/registro")
    public ResponseEntity<UsuarioDTO> registro(@RequestBody @Valid RegistroDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(dto));
    }

    @GetMapping("/me")
    public UsuarioDTO me(Authentication auth) {
        return service.me(auth.getName());
    }
}
