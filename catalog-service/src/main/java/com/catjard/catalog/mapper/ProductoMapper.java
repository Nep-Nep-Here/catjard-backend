package com.catjard.catalog.mapper;

import com.catjard.catalog.dto.CrearProductoDTO;
import com.catjard.catalog.dto.ProductoDTO;
import com.catjard.catalog.model.Producto;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;

@Component
public class ProductoMapper {

    public ProductoDTO toDTO(Producto p) {
        return new ProductoDTO(
                p.getId(),
                p.getSlug(),
                p.getNombre(),
                p.getCategoria().name(),
                p.getPrecio(),
                p.getStock(),
                p.getStockMinimo(),
                p.getDescripcion(),
                p.getImagenUrl(),
                List.copyOf(p.getTecnicas())
        );
    }

    public Producto fromCrear(CrearProductoDTO dto) {
        return Producto.builder()
                .slug(dto.slug())
                .nombre(dto.nombre())
                .categoria(dto.categoria())
                .precio(dto.precio())
                .stock(dto.stock())
                .stockMinimo(dto.stockMinimo())
                .descripcion(dto.descripcion())
                .imagenUrl(dto.imagenUrl())
                .tecnicas(new HashSet<>(dto.tecnicas()))
                .build();
    }
}
