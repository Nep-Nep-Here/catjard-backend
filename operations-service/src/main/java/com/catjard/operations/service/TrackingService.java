package com.catjard.operations.service;

import com.catjard.operations.dto.MarcarHitoDTO;
import com.catjard.operations.dto.TrackingEventoDTO;
import com.catjard.operations.mapper.TrackingMapper;
import com.catjard.operations.model.HitoTracking;
import com.catjard.operations.model.TrackingEvento;
import com.catjard.operations.repository.TrackingEventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrackingService {

    private final TrackingEventoRepository repo;

    @Transactional(readOnly = true)
    public List<TrackingEventoDTO> listar(String pedidoCodigo) {
        return repo.findByPedidoCodigoOrderByIdAsc(pedidoCodigo).stream()
                .map(TrackingMapper::toDTO).toList();
    }

    @Transactional
    public TrackingEventoDTO marcar(String pedidoCodigo, MarcarHitoDTO dto) {
        HitoTracking hito = parseHito(dto.hito());
        TrackingEvento ev = repo.findByPedidoCodigoAndHito(pedidoCodigo, hito)
                .orElseGet(() -> TrackingEvento.builder()
                        .pedidoCodigo(pedidoCodigo)
                        .hito(hito)
                        .completo(Boolean.FALSE)
                        .build());

        ev.setFecha(dto.fecha() != null ? dto.fecha() : LocalDate.now());
        ev.setCompleto(Boolean.TRUE);
        if (dto.observacion() != null) ev.setObservacion(dto.observacion());

        return TrackingMapper.toDTO(repo.save(ev));
    }

    @Transactional
    void registrarHitoSilencioso(String pedidoCodigo, HitoTracking hito, LocalDate fecha, String observacion) {
        TrackingEvento ev = repo.findByPedidoCodigoAndHito(pedidoCodigo, hito)
                .orElseGet(() -> TrackingEvento.builder()
                        .pedidoCodigo(pedidoCodigo)
                        .hito(hito)
                        .completo(Boolean.FALSE)
                        .build());
        ev.setFecha(fecha != null ? fecha : LocalDate.now());
        ev.setCompleto(Boolean.TRUE);
        if (observacion != null) ev.setObservacion(observacion);
        repo.save(ev);
    }

    private HitoTracking parseHito(String s) {
        try {
            return HitoTracking.valueOf(s.toLowerCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Hito invalido: " + s);
        }
    }
}
