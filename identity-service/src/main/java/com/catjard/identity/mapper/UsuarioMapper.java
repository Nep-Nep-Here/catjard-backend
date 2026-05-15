package com.catjard.identity.mapper;

import com.catjard.identity.dto.CrearUsuarioDTO;
import com.catjard.identity.dto.RegistroDTO;
import com.catjard.identity.dto.UsuarioDTO;
import com.catjard.identity.model.Rol;
import com.catjard.identity.model.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public UsuarioDTO toDTO(Usuario u) {
        return new UsuarioDTO(
                u.getId(),
                u.getEmail(),
                u.getRol().name(),
                u.getNombre(),
                u.getEmpresa(),
                u.getRuc(),
                u.getTelefono(),
                u.getDireccion(),
                u.getCargo(),
                u.getClienteId()
        );
    }

    public Usuario fromCrear(CrearUsuarioDTO dto, String passwordEncoded) {
        return Usuario.builder()
                .email(dto.email())
                .password(passwordEncoded)
                .rol(dto.rol())
                .nombre(dto.nombre())
                .empresa(dto.empresa())
                .ruc(dto.ruc())
                .telefono(dto.telefono())
                .direccion(dto.direccion())
                .cargo(dto.cargo())
                .clienteId(dto.clienteId())
                .build();
    }

    public Usuario fromRegistro(RegistroDTO dto, String passwordEncoded) {
        return Usuario.builder()
                .email(dto.email())
                .password(passwordEncoded)
                .rol(Rol.cliente)
                .nombre(dto.nombre())
                .empresa(dto.empresa())
                .ruc(dto.ruc())
                .telefono(dto.telefono())
                .direccion(dto.direccion())
                .build();
    }
}
