package com.catjard.inventory.service;

import com.catjard.inventory.client.CatalogClient;
import com.catjard.inventory.dto.CrearMovimientoDTO;
import com.catjard.inventory.dto.MovimientoDTO;
import com.catjard.inventory.mapper.MovimientoMapper;
import com.catjard.inventory.model.Movimiento;
import com.catjard.inventory.model.TipoMovimiento;
import com.catjard.inventory.repository.MovimientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MovimientoService {

    private final MovimientoRepository repo;
    private final CatalogClient catalogClient;

    @Transactional(readOnly = true)
    public List<MovimientoDTO> listar(Optional<String> tipo, Optional<Long> productoId, Optional<String> referencia) {
        List<Movimiento> data;
        if (tipo.isPresent() && !tipo.get().isBlank()) {
            data = repo.findByTipoOrderByFechaDescIdDesc(parseTipo(tipo.get()));
        } else if (productoId.isPresent()) {
            data = repo.findByProductoIdOrderByFechaDescIdDesc(productoId.get());
        } else if (referencia.isPresent() && !referencia.get().isBlank()) {
            data = repo.findByReferenciaOrderByFechaDescIdDesc(referencia.get());
        } else {
            data = repo.findAllByOrderByFechaDescIdDesc();
        }
        return data.stream().map(MovimientoMapper::toDTO).toList();
    }

    @Transactional
    public MovimientoDTO crear(CrearMovimientoDTO dto) {
        TipoMovimiento tipo = parseTipo(dto.tipo());
        Movimiento m = Movimiento.builder()
                .fecha(LocalDate.now())
                .tipo(tipo)
                .productoId(dto.productoId())
                .cantidad(dto.cantidad())
                .motivo(dto.motivo())
                .referencia(dto.referencia())
                .usuario(dto.usuario())
                .notas(dto.notas())
                .build();
        Movimiento saved = repo.save(m);

        // ajuste de stock en catalog-service via Feign
        int delta = switch (tipo) {
            case entrada -> Math.abs(dto.cantidad());
            case salida  -> -Math.abs(dto.cantidad());
            case ajuste  -> dto.cantidad(); // puede ser negativo
        };
        catalogClient.actualizarStock(dto.productoId(), Map.of("delta", delta));

        return MovimientoMapper.toDTO(saved);
    }

    @Transactional
    void registrarSinAjusteStock(TipoMovimiento tipo, Long productoId, int cantidad,
                                 String motivo, String referencia, String usuario, String notas) {
        Movimiento m = Movimiento.builder()
                .fecha(LocalDate.now())
                .tipo(tipo)
                .productoId(productoId)
                .cantidad(cantidad)
                .motivo(motivo)
                .referencia(referencia)
                .usuario(usuario)
                .notas(notas)
                .build();
        repo.save(m);
    }

    private TipoMovimiento parseTipo(String s) {
        try {
            return TipoMovimiento.valueOf(s.toLowerCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Tipo de movimiento invalido: " + s);
        }
    }
}
