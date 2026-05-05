package com.catjard.inventory.mapper;

import com.catjard.inventory.dto.CrearProveedorDTO;
import com.catjard.inventory.dto.ProveedorDTO;
import com.catjard.inventory.model.Proveedor;

public class ProveedorMapper {

    private ProveedorMapper() {}

    public static ProveedorDTO toDTO(Proveedor p) {
        return new ProveedorDTO(
                p.getId(),
                p.getRazonSocial(),
                p.getNombreComercial(),
                p.getRuc(),
                p.getContacto(),
                p.getEmail(),
                p.getTelefono(),
                p.getDireccion(),
                p.getProductos(),
                p.getNotas(),
                p.getActivo(),
                p.getFechaAlta()
        );
    }

    public static Proveedor fromCrear(CrearProveedorDTO dto) {
        return Proveedor.builder()
                .razonSocial(dto.razonSocial())
                .nombreComercial(dto.nombreComercial())
                .ruc(dto.ruc())
                .contacto(dto.contacto())
                .email(dto.email())
                .telefono(dto.telefono())
                .direccion(dto.direccion())
                .productos(dto.productos())
                .notas(dto.notas())
                .activo(dto.activo() != null ? dto.activo() : Boolean.TRUE)
                .build();
    }
}
