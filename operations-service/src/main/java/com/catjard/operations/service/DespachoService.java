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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        if (repo.existsByPedidoCodigo(dto.pedidoCodigo())) {
            throw new IllegalStateException("Pedido ya tiene despacho registrado: " + dto.pedidoCodigo());
        }

        Despacho d = Despacho.builder()
                .pedidoCodigo(dto.pedidoCodigo())
                .courier(dto.courier())
                .guiaRemision(dto.guiaRemision())
                .fechaDespacho(dto.fechaDespacho() != null ? dto.fechaDespacho() : LocalDate.now())
                .direccionEntrega(dto.direccionEntrega())
                .notas(dto.notas())
                .build();
        Despacho saved = repo.save(d);

        trackingService.registrarHitoSilencioso(dto.pedidoCodigo(), HitoTracking.despachado,
                saved.getFechaDespacho(), "Despachado con " + dto.courier());

        Map<String, Object> body = new HashMap<>();
        body.put("estado", "despachado");
        body.put("courier", dto.courier());
        if (dto.guiaRemision() != null) body.put("guiaRemision", dto.guiaRemision());
        try {
            salesClient.actualizarPedidoPorCodigo(dto.pedidoCodigo(), body);
        } catch (Exception ignored) { }

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
        } catch (Exception ignored) { }

        return DespachoMapper.toDTO(d);
    }
}
