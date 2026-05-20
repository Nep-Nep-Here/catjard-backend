package com.catjard.sales.service;

import com.catjard.sales.dto.ActualizarCotizacionDTO;
import com.catjard.sales.dto.CotizacionDTO;
import com.catjard.sales.dto.CrearCotizacionDTO;
import com.catjard.sales.dto.ItemDTO;
import com.catjard.sales.client.MarcarHitoBody;
import com.catjard.sales.client.OperationsTrackingClient;
import com.catjard.sales.mapper.CotizacionMapper;
import com.catjard.sales.model.Cotizacion;
import com.catjard.sales.model.CotizacionItem;
import com.catjard.sales.model.EstadoCotizacion;
import com.catjard.sales.repository.CotizacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CotizacionService {

    private final CotizacionRepository repo;
    private final PedidoService pedidoService;
    private final OperationsTrackingClient operationsTracking;

    @Transactional(readOnly = true)
    public List<CotizacionDTO> listar(Optional<String> estado, Optional<Long> clienteId) {
        List<Cotizacion> data;
        if (estado.isPresent() && !estado.get().isBlank()) {
            data = repo.findByEstado(parseEstado(estado.get()));
        } else if (clienteId.isPresent()) {
            data = repo.findByClienteIdOrderByFechaDescIdDesc(clienteId.get());
        } else {
            data = repo.findAllByOrderByFechaDescIdDesc();
        }
        return data.stream().map(CotizacionMapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public CotizacionDTO obtener(Long id) {
        return repo.findById(id).map(CotizacionMapper::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Cotizacion no encontrada: " + id));
    }

    @Transactional(readOnly = true)
    public CotizacionDTO obtenerPorCodigo(String codigo) {
        return repo.findByCodigo(codigo).map(CotizacionMapper::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Cotizacion no encontrada: " + codigo));
    }

    @Transactional
    public CotizacionDTO crear(CrearCotizacionDTO dto) {
        var totales = TotalesCalculator.calcular(dto.items());

        Cotizacion cot = Cotizacion.builder()
                .codigo(generarCodigo())
                .clienteId(dto.clienteId())
                .empresa(dto.empresa())
                .ruc(dto.ruc())
                .fecha(LocalDate.now())
                .logoNombre(dto.logoNombre())
                .notasCliente(dto.notasCliente())
                .estado(EstadoCotizacion.enviada)
                .subtotal(totales.subtotal())
                .igv(totales.igv())
                .total(totales.total())
                .validez(dto.validez())
                .notasVendedor(dto.notasVendedor())
                .vendedor(dto.vendedor())
                .build();

        for (ItemDTO it : dto.items()) {
            cot.getItems().add(CotizacionMapper.itemFromDTO(cot, it));
        }
        return CotizacionMapper.toDTO(repo.save(cot));
    }

    @Transactional
    public CotizacionDTO actualizar(Long id, ActualizarCotizacionDTO dto) {
        Cotizacion cot = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cotizacion no encontrada: " + id));

        if (cot.getEstado() == EstadoCotizacion.aprobada || cot.getEstado() == EstadoCotizacion.rechazada) {
            // permite editar solo notas / motivo en estados finales
            if (dto.notasVendedor() != null) cot.setNotasVendedor(dto.notasVendedor());
            if (dto.motivoRechazo() != null) cot.setMotivoRechazo(dto.motivoRechazo());
            return CotizacionMapper.toDTO(cot);
        }

        if (dto.logoNombre() != null)    cot.setLogoNombre(dto.logoNombre());
        if (dto.notasCliente() != null)  cot.setNotasCliente(dto.notasCliente());
        if (dto.validez() != null)       cot.setValidez(dto.validez());
        if (dto.notasVendedor() != null) cot.setNotasVendedor(dto.notasVendedor());
        if (dto.vendedor() != null)      cot.setVendedor(dto.vendedor());
        if (dto.motivoRechazo() != null) cot.setMotivoRechazo(dto.motivoRechazo());
        if (dto.estado() != null)        cot.setEstado(parseEstado(dto.estado()));

        if (dto.items() != null && !dto.items().isEmpty()) {
            cot.getItems().clear();
            for (ItemDTO it : dto.items()) {
                cot.getItems().add(CotizacionMapper.itemFromDTO(cot, it));
            }
            var totales = TotalesCalculator.calcular(dto.items());
            cot.setSubtotal(totales.subtotal());
            cot.setIgv(totales.igv());
            cot.setTotal(totales.total());
        }

        return CotizacionMapper.toDTO(cot);
    }

    @Transactional
    public CotizacionDTO aprobar(Long id) {
        Cotizacion cot = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cotizacion no encontrada: " + id));
        if (cot.getEstado() == EstadoCotizacion.aprobada) {
            return CotizacionMapper.toDTO(cot);
        }
        if (cot.getEstado() == EstadoCotizacion.rechazada) {
            throw new IllegalStateException("Cotizacion fue rechazada, no puede aprobarse");
        }
        cot.setEstado(EstadoCotizacion.aprobada);
        cot.setMotivoRechazo(null);

        // generar pedido vinculado
        String pedidoCodigo = pedidoService.crearDesdeCotizacion(cot);
        cot.setPedidoCodigo(pedidoCodigo);

        // marcar el hito de tracking "cotizacion_aprobada" (best-effort)
        try {
            operationsTracking.marcarHito(pedidoCodigo,
                    new MarcarHitoBody("cotizacion_aprobada", LocalDate.now(), "Cotización aprobada"));
        } catch (Exception ex) {
            log.warn("Hito cotizacion_aprobada no marcado para {}: {}", pedidoCodigo, ex.getMessage());
        }

        return CotizacionMapper.toDTO(cot);
    }

    @Transactional
    public CotizacionDTO rechazar(Long id, String motivo) {
        Cotizacion cot = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cotizacion no encontrada: " + id));
        if (cot.getEstado() == EstadoCotizacion.aprobada) {
            throw new IllegalStateException("Cotizacion ya fue aprobada");
        }
        cot.setEstado(EstadoCotizacion.rechazada);
        cot.setMotivoRechazo(motivo);
        return CotizacionMapper.toDTO(cot);
    }

    @Transactional
    public void eliminar(Long id) {
        Cotizacion cot = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cotizacion no encontrada: " + id));
        if (cot.getPedidoCodigo() != null) {
            throw new IllegalStateException("Cotizacion ya tiene pedido asociado, no puede eliminarse");
        }
        repo.delete(cot);
    }

    private EstadoCotizacion parseEstado(String s) {
        try {
            return EstadoCotizacion.valueOf(s.toLowerCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Estado de cotizacion invalido: " + s);
        }
    }

    String generarCodigo() {
        int year = LocalDate.now().getYear();
        String prefijo = "COT-" + year + "-";
        int siguiente = repo.findByCodigoStartingWithOrderByCodigoDesc(prefijo).stream()
                .findFirst()
                .map(c -> {
                    String[] parts = c.getCodigo().split("-");
                    try { return Integer.parseInt(parts[2]) + 1; }
                    catch (Exception e) { return 1; }
                })
                .orElse(1);
        return prefijo + String.format("%04d", siguiente);
    }
}
