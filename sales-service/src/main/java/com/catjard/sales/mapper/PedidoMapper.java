package com.catjard.sales.mapper;

import com.catjard.sales.dto.ItemDTO;
import com.catjard.sales.dto.PedidoDTO;
import com.catjard.sales.model.Pedido;
import com.catjard.sales.model.PedidoItem;

import java.util.List;

public class PedidoMapper {

    private PedidoMapper() {}

    public static PedidoDTO toDTO(Pedido p) {
        List<ItemDTO> items = p.getItems().stream()
                .map(PedidoMapper::itemToDTO)
                .toList();
        return new PedidoDTO(
                p.getId(),
                p.getCodigo(),
                p.getCotizacionCodigo(),
                p.getClienteId(),
                p.getEmpresa(),
                p.getFechaPedido(),
                p.getFechaEntregaEstimada(),
                p.getSubtotal(),
                p.getIgv(),
                p.getTotal(),
                p.getVoucherUrl(),
                p.getVoucherFecha(),
                p.getEstado() != null ? p.getEstado().name() : null,
                p.getCourier(),
                p.getGuiaRemision(),
                p.getProcesadoPor(),
                items
        );
    }

    public static ItemDTO itemToDTO(PedidoItem i) {
        return new ItemDTO(i.getProductoId(), i.getCantidad(), i.getPrecioUnit(), i.getTecnica(), null);
    }

    public static PedidoItem itemFromDTO(Pedido p, ItemDTO d) {
        return PedidoItem.builder()
                .pedido(p)
                .productoId(d.productoId())
                .cantidad(d.cantidad())
                .precioUnit(d.precioUnit())
                .tecnica(d.tecnica())
                .build();
    }
}
