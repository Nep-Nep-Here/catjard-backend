package com.catjard.catalog.mapper;

import com.catjard.catalog.dto.CrearPromocionDTO;
import com.catjard.catalog.dto.PromocionDTO;
import com.catjard.catalog.model.AplicaA;
import com.catjard.catalog.model.Promocion;
import org.springframework.stereotype.Component;

@Component
public class PromocionMapper {

    public PromocionDTO toDTO(Promocion p) {
        String aplicaValor = switch (p.getAplicaA()) {
            case todo -> null;
            case categoria -> p.getCategoriaAplica() == null ? null : p.getCategoriaAplica().name();
            case producto -> p.getProductoId() == null ? null : String.valueOf(p.getProductoId());
        };
        return new PromocionDTO(
                p.getId(),
                p.getNombre(),
                p.getDescripcion(),
                p.getDescuentoPct(),
                p.getDesde(),
                p.getHasta(),
                p.getAplicaA().name(),
                aplicaValor,
                p.getActiva()
        );
    }

    public Promocion fromCrear(CrearPromocionDTO dto) {
        return Promocion.builder()
                .nombre(dto.nombre())
                .descripcion(dto.descripcion())
                .descuentoPct(dto.descuentoPct())
                .desde(dto.desde())
                .hasta(dto.hasta())
                .aplicaA(dto.aplicaA())
                .categoriaAplica(dto.aplicaA() == AplicaA.categoria ? dto.categoriaAplica() : null)
                .productoId(dto.aplicaA() == AplicaA.producto ? dto.productoId() : null)
                .activa(dto.activa())
                .build();
    }
}
