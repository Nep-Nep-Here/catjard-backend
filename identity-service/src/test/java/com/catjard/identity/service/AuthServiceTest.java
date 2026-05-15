package com.catjard.identity.service;

import com.catjard.identity.dto.LoginRequest;
import com.catjard.identity.dto.LoginResponse;
import com.catjard.identity.dto.RegistroDTO;
import com.catjard.identity.dto.UsuarioDTO;
import com.catjard.identity.mapper.UsuarioMapper;
import com.catjard.identity.model.Rol;
import com.catjard.identity.model.Usuario;
import com.catjard.identity.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios para AuthService.
 * Cubre los mecanismos de seguridad: BCrypt (verificacion / hash) y emision de JWT.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UsuarioRepository repo;
    @Mock PasswordEncoder encoder;
    @Mock JwtService jwt;
    @Mock UsuarioMapper mapper;

    @InjectMocks AuthService service;

    private Usuario usuarioMock;
    private UsuarioDTO usuarioDTOMock;

    @BeforeEach
    void setUp() {
        usuarioMock = Usuario.builder()
                .id(1L)
                .email("cliente@empresa.com")
                .password("$2a$10$hashFalsoBCrypt")
                .rol(Rol.cliente)
                .nombre("Lucia Montoya")
                .build();
        usuarioDTOMock = new UsuarioDTO(
                1L, "cliente@empresa.com", "cliente", "Lucia Montoya",
                null, null, null, null, null, null
        );
    }

    @Test
    @DisplayName("login OK: devuelve LoginResponse con token cuando credenciales son correctas")
    void loginOk() {
        var req = new LoginRequest("cliente@empresa.com", "cliente123");
        when(repo.findByEmailIgnoreCase("cliente@empresa.com")).thenReturn(Optional.of(usuarioMock));
        when(encoder.matches("cliente123", "$2a$10$hashFalsoBCrypt")).thenReturn(true);
        when(jwt.generate(usuarioMock)).thenReturn("token-jwt-123");
        when(jwt.getExpirationMs()).thenReturn(86_400_000L);
        when(mapper.toDTO(usuarioMock)).thenReturn(usuarioDTOMock);

        LoginResponse resp = service.login(req);

        assertThat(resp.token()).isEqualTo("token-jwt-123");
        assertThat(resp.expiresInMs()).isEqualTo(86_400_000L);
        assertThat(resp.user().email()).isEqualTo("cliente@empresa.com");
        verify(jwt).generate(usuarioMock);
    }

    @Test
    @DisplayName("login KO: BadCredentialsException cuando el email no existe")
    void loginEmailNoExiste() {
        var req = new LoginRequest("noexiste@empresa.com", "loquesea");
        when(repo.findByEmailIgnoreCase("noexiste@empresa.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(req))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Credenciales");

        verify(jwt, never()).generate(any());
    }

    @Test
    @DisplayName("login KO: BadCredentialsException cuando la contrasena es incorrecta")
    void loginPasswordIncorrecta() {
        var req = new LoginRequest("cliente@empresa.com", "passwordMala");
        when(repo.findByEmailIgnoreCase("cliente@empresa.com")).thenReturn(Optional.of(usuarioMock));
        when(encoder.matches("passwordMala", "$2a$10$hashFalsoBCrypt")).thenReturn(false);

        assertThatThrownBy(() -> service.login(req))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Credenciales");

        verify(jwt, never()).generate(any());
    }

    @Test
    @DisplayName("registrar OK: hashea la contrasena con BCrypt y persiste el usuario")
    void registrarOk() {
        var dto = new RegistroDTO(
                "nuevo@empresa.com", "password123", "Carlos Diaz",
                "Acme S.A.C.", "20512345671", "+51 999111222", "Av. Demo 123"
        );
        when(repo.existsByEmailIgnoreCase("nuevo@empresa.com")).thenReturn(false);
        when(encoder.encode("password123")).thenReturn("$2a$10$hashGenerado");

        var nuevo = Usuario.builder().id(7L).email("nuevo@empresa.com").rol(Rol.cliente).build();
        when(mapper.fromRegistro(eq(dto), eq("$2a$10$hashGenerado"))).thenReturn(nuevo);
        when(repo.save(nuevo)).thenReturn(nuevo);

        var dtoSalida = new UsuarioDTO(
                7L, "nuevo@empresa.com", "cliente", "Carlos Diaz",
                "Acme S.A.C.", "20512345671", "+51 999111222", "Av. Demo 123", null, null
        );
        when(mapper.toDTO(nuevo)).thenReturn(dtoSalida);

        UsuarioDTO resultado = service.registrar(dto);

        assertThat(resultado.id()).isEqualTo(7L);
        assertThat(resultado.role()).isEqualTo("cliente");
        // El password en claro NUNCA se persiste; solo se almacena el hash.
        verify(encoder).encode("password123");
        verify(repo).save(nuevo);
    }

    @Test
    @DisplayName("registrar KO: IllegalArgumentException si el email ya existe")
    void registrarEmailDuplicado() {
        var dto = new RegistroDTO(
                "cliente@empresa.com", "password123", "Otro Cliente",
                "Empresa X", "20512345672", null, null
        );
        when(repo.existsByEmailIgnoreCase("cliente@empresa.com")).thenReturn(true);

        assertThatThrownBy(() -> service.registrar(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe un usuario");

        verify(encoder, never()).encode(any());
        verify(repo, never()).save(any());
    }
}
