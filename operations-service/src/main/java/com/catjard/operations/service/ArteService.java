package com.catjard.operations.service;

import com.catjard.operations.client.SalesClient;
import com.catjard.operations.dto.ArteDTO;
import com.catjard.operations.dto.RevisionArteDTO;
import com.catjard.operations.dto.SubirArteDTO;
import com.catjard.operations.mapper.ArteMapper;
import com.catjard.operations.model.Arte;
import com.catjard.operations.model.EstadoArte;
import com.catjard.operations.model.HitoTracking;
import com.catjard.operations.repository.ArteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ArteService {

    private final ArteRepository repo;
    private final TrackingService trackingService;
    private final SalesClient salesClient;

    @Transactional(readOnly = true)
    public List<ArteDTO> listarPorPedido(String pedidoCodigo) {
        return repo.findByPedidoCodigoOrderByVersionAsc(pedidoCodigo).stream()
                .map(ArteMapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public ArteDTO obtener(Long id) {
        return repo.findById(id).map(ArteMapper::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Arte no encontrado: " + id));
    }

    @Transactional
    public ArteDTO subirNuevaVersion(String pedidoCodigo, SubirArteDTO dto) {
        int siguienteVersion = repo.findFirstByPedidoCodigoOrderByVersionDesc(pedidoCodigo)
                .map(a -> a.getVersion() + 1)
                .orElse(1);

        Arte arte = Arte.builder()
                .pedidoCodigo(pedidoCodigo)
                .version(siguienteVersion)
                .nombreArchivo(dto.nombreArchivo())
                .fecha(LocalDate.now())
                .estado(EstadoArte.en_revision)
                .build();
        Arte saved = repo.save(arte);

        if (siguienteVersion == 1) {
            trackingService.registrarHitoSilencioso(pedidoCodigo, HitoTracking.en_diseno, LocalDate.now(),
                    "Primera version de arte enviada al cliente.");
            try {
                salesClient.actualizarPedidoPorCodigo(pedidoCodigo, Map.of("estado", "en_diseno"));
            } catch (Exception ignored) { }
        }
        return ArteMapper.toDTO(saved);
    }

    @Transactional
    public ArteDTO aprobar(Long arteId, RevisionArteDTO dto) {
        Arte arte = repo.findById(arteId)
                .orElseThrow(() -> new IllegalArgumentException("Arte no encontrado: " + arteId));
        arte.setEstado(EstadoArte.aprobado);
        if (dto != null && dto.comentariosCliente() != null) {
            arte.setComentariosCliente(dto.comentariosCliente());
        }

        trackingService.registrarHitoSilencioso(arte.getPedidoCodigo(), HitoTracking.arte_aprobado, LocalDate.now(),
                "Arte v" + arte.getVersion() + " aprobado.");
        try {
            salesClient.actualizarPedidoPorCodigo(arte.getPedidoCodigo(), Map.of("estado", "en_produccion"));
        } catch (Exception ignored) { }

        return ArteMapper.toDTO(arte);
    }

    @Transactional
    public ArteDTO rechazar(Long arteId, RevisionArteDTO dto) {
        Arte arte = repo.findById(arteId)
                .orElseThrow(() -> new IllegalArgumentException("Arte no encontrado: " + arteId));
        arte.setEstado(EstadoArte.rechazado);
        if (dto != null && dto.comentariosCliente() != null) {
            arte.setComentariosCliente(dto.comentariosCliente());
        }
        return ArteMapper.toDTO(arte);
    }
}
