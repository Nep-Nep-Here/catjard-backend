package com.catjard.crm.mapper;

import com.catjard.crm.dto.CrearLeadDTO;
import com.catjard.crm.dto.LeadDTO;
import com.catjard.crm.model.EstadoLead;
import com.catjard.crm.model.Lead;

public class LeadMapper {

    private LeadMapper() {}

    public static LeadDTO toDTO(Lead l) {
        return new LeadDTO(
                l.getId(),
                l.getCodigo(),
                l.getFecha(),
                l.getNombre(),
                l.getEmpresa(),
                l.getRuc(),
                l.getEmail(),
                l.getTelefono(),
                l.getProductos(),
                l.getCantidad(),
                l.getMensaje(),
                l.getEstado() != null ? l.getEstado().name() : null,
                l.getAsignadoA(),
                l.getNotasInternas()
        );
    }

    public static Lead fromCrear(CrearLeadDTO dto) {
        return Lead.builder()
                .nombre(dto.nombre())
                .empresa(dto.empresa())
                .ruc(dto.ruc())
                .email(dto.email())
                .telefono(dto.telefono())
                .productos(dto.productos())
                .cantidad(dto.cantidad())
                .mensaje(dto.mensaje())
                .estado(EstadoLead.nuevo)
                .build();
    }
}
