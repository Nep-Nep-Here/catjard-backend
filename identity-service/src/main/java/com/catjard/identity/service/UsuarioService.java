package com.catjard.identity.service;

import com.catjard.identity.dto.ActualizarUsuarioDTO;
import com.catjard.identity.dto.CrearClienteUsuarioDTO;
import com.catjard.identity.dto.CrearUsuarioDTO;
import com.catjard.identity.dto.UsuarioDTO;
import com.catjard.identity.mapper.UsuarioMapper;
import com.catjard.identity.model.Rol;
import com.catjard.identity.model.Usuario;
import com.catjard.identity.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repo;
    private final UsuarioMapper mapper;
    private final PasswordEncoder encoder;

    public List<UsuarioDTO> listar() {
        return repo.findAll().stream().map(mapper::toDTO).toList();
    }

    public UsuarioDTO obtener(Long id) {
        return repo.findById(id).map(mapper::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));
    }

    @Transactional
    public UsuarioDTO crear(CrearUsuarioDTO dto) {
        if (repo.existsByEmailIgnoreCase(dto.email())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese email.");
        }
        Usuario u = mapper.fromCrear(dto, encoder.encode(dto.password()));
        return mapper.toDTO(repo.save(u));
    }

    @Transactional
    public UsuarioDTO crearClienteDesdeLead(CrearClienteUsuarioDTO dto) {
        if (repo.existsByEmailIgnoreCase(dto.email())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese email.");
        }
        Usuario u = Usuario.builder()
                .email(dto.email())
                .password(encoder.encode(dto.password()))
                .rol(Rol.cliente)
                .nombre(dto.nombre())
                .empresa(dto.empresa())
                .ruc(dto.ruc())
                .telefono(dto.telefono())
                .direccion(dto.direccion())
                .clienteId(dto.clienteId())
                .build();
        return mapper.toDTO(repo.save(u));
    }

    @Transactional
    public UsuarioDTO actualizar(Long id, ActualizarUsuarioDTO dto) {
        Usuario u = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));
        if (dto.nombre() != null) u.setNombre(dto.nombre());
        if (dto.empresa() != null) u.setEmpresa(dto.empresa());
        if (dto.ruc() != null) u.setRuc(dto.ruc());
        if (dto.telefono() != null) u.setTelefono(dto.telefono());
        if (dto.direccion() != null) u.setDireccion(dto.direccion());
        if (dto.cargo() != null) u.setCargo(dto.cargo());
        return mapper.toDTO(u);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!repo.existsById(id)) {
            throw new IllegalArgumentException("Usuario no encontrado: " + id);
        }
        repo.deleteById(id);
    }
}
