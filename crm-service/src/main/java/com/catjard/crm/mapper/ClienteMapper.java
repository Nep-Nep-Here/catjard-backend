package com.catjard.crm.mapper;

import com.catjard.crm.dto.ClienteDTO;
import com.catjard.crm.dto.CrearClienteDTO;
import com.catjard.crm.model.ClienteCRM;

public class ClienteMapper {

    private ClienteMapper() {}

    public static ClienteDTO toDTO(ClienteCRM c) {
        return new ClienteDTO(
                c.getId(),
                c.getRazonSocial(),
                c.getNombreComercial(),
                c.getRuc(),
                c.getIndustria(),
                c.getContactoPrincipal(),
                c.getEmail(),
                c.getTelefono(),
                c.getDireccion(),
                c.getCuentaActiva(),
                c.getFechaAlta(),
                c.getNotas()
        );
    }

    public static ClienteCRM fromCrear(CrearClienteDTO dto) {
        return ClienteCRM.builder()
                .razonSocial(dto.razonSocial())
                .nombreComercial(dto.nombreComercial())
                .ruc(dto.ruc())
                .industria(dto.industria())
                .contactoPrincipal(dto.contactoPrincipal())
                .email(dto.email())
                .telefono(dto.telefono())
                .direccion(dto.direccion())
                .cuentaActiva(dto.cuentaActiva() != null ? dto.cuentaActiva() : Boolean.TRUE)
                .notas(dto.notas())
                .build();
    }
}
