package com.catjard.identity.service;

import com.catjard.identity.dto.*;
import com.catjard.identity.mapper.UsuarioMapper;
import com.catjard.identity.model.Usuario;
import com.catjard.identity.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final UsuarioMapper mapper;

    public LoginResponse login(LoginRequest req) {
        Usuario u = repo.findByEmailIgnoreCase(req.email())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas. Verifica tu correo y contraseña."));
        if (!encoder.matches(req.password(), u.getPassword())) {
            throw new BadCredentialsException("Credenciales inválidas. Verifica tu correo y contraseña.");
        }
        return new LoginResponse(jwt.generate(u), jwt.getExpirationMs(), mapper.toDTO(u));
    }

    @Transactional
    public UsuarioDTO registrar(RegistroDTO dto) {
        if (repo.existsByEmailIgnoreCase(dto.email())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese email.");
        }
        Usuario nuevo = mapper.fromRegistro(dto, encoder.encode(dto.password()));
        return mapper.toDTO(repo.save(nuevo));
    }

    public UsuarioDTO me(String email) {
        return repo.findByEmailIgnoreCase(email)
                .map(mapper::toDTO)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado: " + email));
    }
}
