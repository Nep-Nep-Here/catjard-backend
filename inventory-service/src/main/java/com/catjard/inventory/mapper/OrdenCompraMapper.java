package com.catjard.inventory.mapper;

import com.catjard.inventory.dto.OCItemDTO;
import com.catjard.inventory.dto.OrdenCompraDTO;
import com.catjard.inventory.model.OrdenCompra;
import com.catjard.inventory.model.OrdenCompraItem;

import java.util.List;

public class OrdenCompraMapper {

    private OrdenCompraMapper() {}

    public static OrdenCompraDTO toDTO(OrdenCompra oc) {
        List<OCItemDTO> items = oc.getItems().stream()
                .map(OrdenCompraMapper::itemToDTO)
                .toList();
        return new OrdenCompraDTO(
                oc.getId(),
                oc.getCodigo(),
                oc.getFecha(),
                oc.getProveedorId(),
                oc.getProveedorNombre(),
                oc.getEstado() != null ? oc.getEstado().name() : null,
                oc.getFechaEsperada(),
                oc.getFechaRecepcion(),
                oc.getSubtotal(),
                oc.getIgv(),
                oc.getTotal(),
                oc.getUsuario(),
                oc.getNotas(),
                items
        );
    }

    public static OCItemDTO itemToDTO(OrdenCompraItem i) {
        return new OCItemDTO(i.getProductoId(), i.getCantidad(), i.getPrecioUnit());
    }

    public static OrdenCompraItem itemFromDTO(OrdenCompra oc, OCItemDTO d) {
        return OrdenCompraItem.builder()
                .ordenCompra(oc)
                .productoId(d.productoId())
                .cantidad(d.cantidad())
                .precioUnit(d.precioUnit())
                .build();
    }
}
