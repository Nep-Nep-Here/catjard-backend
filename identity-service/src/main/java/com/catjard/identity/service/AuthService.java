package com.catjard.identity.service;

import com.catjard.identity.client.CrmClient;
import com.catjard.identity.dto.*;
import com.catjard.identity.mapper.UsuarioMapper;
import com.catjard.identity.model.Usuario;
import com.catjard.identity.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final UsuarioMapper mapper;
    private final CrmClient crmClient;

    // MECANISMO DE SEGURIDAD: BCrypt (verificación de contraseña) + emisión de JWT.
    // Compara el password en claro contra el hash BCrypt almacenado en BD; si coincide
    // emite un JWT firmado para autenticar las próximas requests.
    public LoginResponse login(LoginRequest req) {
        Usuario u = repo.findByEmailIgnoreCase(req.email())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas. Verifica tu correo y contraseña."));
        // BCrypt compara con el hash de forma segura (constant-time, anti timing-attack).
        if (!encoder.matches(req.password(), u.getPassword())) {
            throw new BadCredentialsException("Credenciales inválidas. Verifica tu correo y contraseña.");
        }
        return new LoginResponse(jwt.generate(u), jwt.getExpirationMs(), mapper.toDTO(u));
    }

    // MECANISMO DE SEGURIDAD: hash BCrypt al guardar la contraseña.
    // El password en claro NUNCA se persiste; solo se almacena su hash con sal aleatoria.
    @Transactional
    public UsuarioDTO registrar(RegistroDTO dto) {
        if (repo.existsByEmailIgnoreCase(dto.email())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese email.");
        }
        Usuario nuevo = mapper.fromRegistro(dto, encoder.encode(dto.password())); // hash BCrypt

        // Enlaza el usuario con su ClienteCRM (crea o reutiliza por RUC). Sin este
        // clienteId el portal de cliente no tiene datos. Best-effort: si crm-service
        // falla, se crea el usuario igual (clienteId null) y se podrá enlazar luego;
        // mejor eso que perder el registro por un fallo transitorio.
        try {
            ClienteRefDTO cliente = crmClient.registrarOReutilizar(new CrearClienteCrmDTO(
                    dto.empresa(),      // razonSocial
                    dto.empresa(),      // nombreComercial
                    dto.ruc(),
                    null,               // industria (desconocida en el alta web)
                    dto.nombre(),       // contactoPrincipal
                    dto.email(),
                    dto.telefono(),
                    dto.direccion(),
                    Boolean.TRUE,
                    "Alta por auto-registro web"
            ));
            if (cliente != null) nuevo.setClienteId(cliente.id());
        } catch (Exception ex) {
            log.warn("Auto-registro {}: no se pudo crear/enlazar ClienteCRM: {}",
                    dto.email(), ex.getMessage());
        }

        return mapper.toDTO(repo.save(nuevo));
    }

    public UsuarioDTO me(String email) {
        return repo.findByEmailIgnoreCase(email)
                .map(mapper::toDTO)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado: " + email));
    }
}
