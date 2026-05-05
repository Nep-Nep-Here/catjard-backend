package com.catjard.sales.mapper;

import com.catjard.sales.dto.CotizacionDTO;
import com.catjard.sales.dto.ItemDTO;
import com.catjard.sales.model.Cotizacion;
import com.catjard.sales.model.CotizacionItem;

import java.util.List;

public class CotizacionMapper {

    private CotizacionMapper() {}

    public static CotizacionDTO toDTO(Cotizacion c) {
        List<ItemDTO> items = c.getItems().stream()
                .map(CotizacionMapper::itemToDTO)
                .toList();
        return new CotizacionDTO(
                c.getId(),
                c.getCodigo(),
                c.getClienteId(),
                c.getEmpresa(),
                c.getRuc(),
                c.getFecha(),
                c.getLogoNombre(),
                c.getNotasCliente(),
                c.getEstado() != null ? c.getEstado().name() : null,
                c.getSubtotal(),
                c.getIgv(),
                c.getTotal(),
                c.getValidez(),
                c.getNotasVendedor(),
                c.getVendedor(),
                c.getMotivoRechazo(),
                c.getPedidoCodigo(),
                items
        );
    }

    public static ItemDTO itemToDTO(CotizacionItem i) {
        return new ItemDTO(i.getProductoId(), i.getCantidad(), i.getPrecioUnit(), i.getTecnica(), i.getNotas());
    }

    public static CotizacionItem itemFromDTO(Cotizacion c, ItemDTO d) {
        return CotizacionItem.builder()
                .cotizacion(c)
                .productoId(d.productoId())
                .cantidad(d.cantidad())
                .precioUnit(d.precioUnit())
                .tecnica(d.tecnica())
                .notas(d.notas())
                .build();
    }
}
