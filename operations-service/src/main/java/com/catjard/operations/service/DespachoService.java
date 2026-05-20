package com.catjard.operations.service;

import com.catjard.operations.client.SalesClient;
import com.catjard.operations.dto.CrearDespachoDTO;
import com.catjard.operations.dto.DespachoDTO;
import com.catjard.operations.dto.EntregarDTO;
import com.catjard.operations.mapper.DespachoMapper;
import com.catjard.operations.model.Despacho;
import com.catjard.operations.model.HitoTracking;
import com.catjard.operations.repository.DespachoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DespachoService {

    private static final List<String> COURIERS_VALIDOS = List.of("Olva Courier", "Shalom");

    private final DespachoRepository repo;
    private final TrackingService trackingService;
    private final SalesClient salesClient;

    @Transactional(readOnly = true)
    public List<DespachoDTO> listar() {
        return repo.findAllByOrderByFechaDespachoDescIdDesc().stream()
                .map(DespachoMapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public DespachoDTO obtenerPorPedido(String pedidoCodigo) {
        return repo.findByPedidoCodigo(pedidoCodigo).map(DespachoMapper::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Despacho no encontrado para pedido: " + pedidoCodigo));
    }

    @Transactional
    public DespachoDTO crear(CrearDespachoDTO dto) {
        if (!COURIERS_VALIDOS.contains(dto.courier())) {
            throw new IllegalArgumentException("Courier no valido. Permitidos: " + COURIERS_VALIDOS);
        }

        // Idempotente: si el pedido ya tiene despacho, no falla; reutiliza el
        // existente y vuelve a sincronizar el estado del pedido (por si la 1a
        // vez el PATCH a sales fallo y quedo pegado en "listo").
        Despacho saved = repo.findByPedidoCodigo(dto.pedidoCodigo())
                .orElseGet(() -> repo.save(Despacho.builder()
                        .pedidoCodigo(dto.pedidoCodigo())
                        .courier(dto.courier())
                        .guiaRemision(dto.guiaRemision())
                        .fechaDespacho(dto.fechaDespacho() != null ? dto.fechaDespacho() : LocalDate.now())
                        .direccionEntrega(dto.direccionEntrega())
                        .notas(dto.notas())
                        .build()));

        trackingService.registrarHitoSilencioso(dto.pedidoCodigo(), HitoTracking.despachado,
                saved.getFechaDespacho(), "Despachado con " + saved.getCourier());

        Map<String, Object> body = new HashMap<>();
        body.put("estado", "despachado");
        body.put("courier", saved.getCourier());
        if (saved.getGuiaRemision() != null) body.put("guiaRemision", saved.getGuiaRemision());
        try {
            salesClient.actualizarPedidoPorCodigo(dto.pedidoCodigo(), body);
        } catch (Exception e) {
            log.error("Fallo al sincronizar estado 'despachado' del pedido {} con sales-service: {}",
                    dto.pedidoCodigo(), e.toString(), e);
            throw new IllegalStateException(
                    "No se pudo actualizar el estado del pedido " + dto.pedidoCodigo()
                            + " en sales-service: " + e.getMessage(), e);
        }

        return DespachoMapper.toDTO(saved);
    }

    @Transactional
    public DespachoDTO marcarEntregado(String pedidoCodigo, EntregarDTO dto) {
        Despacho d = repo.findByPedidoCodigo(pedidoCodigo)
                .orElseThrow(() -> new IllegalArgumentException("Despacho no encontrado para pedido: " + pedidoCodigo));

        d.setFechaEntregaReal(dto.fechaEntregaReal() != null ? dto.fechaEntregaReal() : LocalDate.now());
        if (dto.receptor() != null) d.setReceptor(dto.receptor());
        if (dto.notas() != null) d.setNotas(dto.notas());

        trackingService.registrarHitoSilencioso(pedidoCodigo, HitoTracking.entregado,
                d.getFechaEntregaReal(), "Entregado a " + Optional.ofNullable(dto.receptor()).orElse("destinatario"));

        try {
            salesClient.actualizarPedidoPorCodigo(pedidoCodigo, Map.of("estado", "entregado"));
        } catch (Exception e) {
            log.error("Fallo al sincronizar estado 'entregado' del pedido {} con sales-service: {}",
                    pedidoCodigo, e.toString(), e);
            throw new IllegalStateException(
                    "No se pudo actualizar el estado del pedido " + pedidoCodigo
                            + " en sales-service: " + e.getMessage(), e);
        }

        return DespachoMapper.toDTO(d);
    }
}
