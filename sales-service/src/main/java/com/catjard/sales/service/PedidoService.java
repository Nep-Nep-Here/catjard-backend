package com.catjard.sales.service;

import com.catjard.sales.dto.ActualizarPedidoDTO;
import com.catjard.sales.dto.PedidoDTO;
import com.catjard.sales.dto.VoucherDTO;
import com.catjard.sales.mapper.PedidoMapper;
import com.catjard.sales.model.Cotizacion;
import com.catjard.sales.model.CotizacionItem;
import com.catjard.sales.model.EstadoPedido;
import com.catjard.sales.model.Pedido;
import com.catjard.sales.model.PedidoItem;
import com.catjard.sales.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository repo;

    @Transactional(readOnly = true)
    public List<PedidoDTO> listar(Optional<String> estado, Optional<Long> clienteId) {
        List<Pedido> data;
        if (estado.isPresent() && !estado.get().isBlank()) {
            data = repo.findByEstado(parseEstado(estado.get()));
        } else if (clienteId.isPresent()) {
            data = repo.findByClienteIdOrderByFechaPedidoDescIdDesc(clienteId.get());
        } else {
            data = repo.findAllByOrderByFechaPedidoDescIdDesc();
        }
        return data.stream().map(PedidoMapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public PedidoDTO obtener(Long id) {
        return repo.findById(id).map(PedidoMapper::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public PedidoDTO obtenerPorCodigo(String codigo) {
        return repo.findByCodigo(codigo).map(PedidoMapper::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado: " + codigo));
    }

    @Transactional
    public String crearDesdeCotizacion(Cotizacion cot) {
        Optional<Pedido> existente = repo.findByCotizacionCodigo(cot.getCodigo());
        if (existente.isPresent()) {
            return existente.get().getCodigo();
        }

        Pedido p = Pedido.builder()
                .codigo(generarCodigo())
                .cotizacionCodigo(cot.getCodigo())
                .clienteId(cot.getClienteId())
                .empresa(cot.getEmpresa())
                .fechaPedido(LocalDate.now())
                .subtotal(cot.getSubtotal())
                .igv(cot.getIgv())
                .total(cot.getTotal())
                .estado(EstadoPedido.en_diseno)
                .build();

        for (CotizacionItem ci : cot.getItems()) {
            p.getItems().add(PedidoItem.builder()
                    .pedido(p)
                    .productoId(ci.getProductoId())
                    .cantidad(ci.getCantidad())
                    .precioUnit(ci.getPrecioUnit())
                    .tecnica(ci.getTecnica())
                    .build());
        }
        return repo.save(p).getCodigo();
    }

    @Transactional
    public PedidoDTO actualizar(Long id, ActualizarPedidoDTO dto) {
        Pedido p = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado: " + id));
        return aplicarCambios(p, dto);
    }

    @Transactional
    public PedidoDTO actualizarPorCodigo(String codigo, ActualizarPedidoDTO dto) {
        Pedido p = repo.findByCodigo(codigo)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado: " + codigo));
        return aplicarCambios(p, dto);
    }

    private PedidoDTO aplicarCambios(Pedido p, ActualizarPedidoDTO dto) {
        if (dto.estado() != null)                  p.setEstado(parseEstado(dto.estado()));
        if (dto.fechaEntregaEstimada() != null)    p.setFechaEntregaEstimada(dto.fechaEntregaEstimada());
        if (dto.voucherUrl() != null)              p.setVoucherUrl(dto.voucherUrl());
        if (dto.voucherFecha() != null)            p.setVoucherFecha(dto.voucherFecha());
        if (dto.courier() != null)                 p.setCourier(dto.courier());
        if (dto.guiaRemision() != null)            p.setGuiaRemision(dto.guiaRemision());
        return PedidoMapper.toDTO(p);
    }

    @Transactional
    public PedidoDTO subirVoucher(Long id, VoucherDTO dto) {
        Pedido p = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado: " + id));
        p.setVoucherUrl(dto.voucherUrl());
        p.setVoucherFecha(LocalDate.now());
        return PedidoMapper.toDTO(p);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!repo.existsById(id)) {
            throw new IllegalArgumentException("Pedido no encontrado: " + id);
        }
        repo.deleteById(id);
    }

    private EstadoPedido parseEstado(String s) {
        try {
            return EstadoPedido.valueOf(s.toLowerCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Estado de pedido invalido: " + s);
        }
    }

    String generarCodigo() {
        int year = LocalDate.now().getYear();
        String prefijo = "PED-" + year + "-";
        int siguiente = repo.findByCodigoStartingWithOrderByCodigoDesc(prefijo).stream()
                .findFirst()
                .map(p -> {
                    String[] parts = p.getCodigo().split("-");
                    try { return Integer.parseInt(parts[2]) + 1; }
                    catch (Exception e) { return 1; }
                })
                .orElse(1);
        return prefijo + String.format("%04d", siguiente);
    }
}
