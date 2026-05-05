package com.catjard.inventory.service;

import com.catjard.inventory.client.CatalogClient;
import com.catjard.inventory.dto.CrearOrdenCompraDTO;
import com.catjard.inventory.dto.OCItemDTO;
import com.catjard.inventory.dto.OrdenCompraDTO;
import com.catjard.inventory.mapper.OrdenCompraMapper;
import com.catjard.inventory.model.*;
import com.catjard.inventory.repository.MovimientoRepository;
import com.catjard.inventory.repository.OrdenCompraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrdenCompraService {

    private static final BigDecimal IGV_RATE = new BigDecimal("0.18");

    private final OrdenCompraRepository repo;
    private final MovimientoRepository movRepo;
    private final ProveedorService proveedorService;
    private final CatalogClient catalogClient;

    @Transactional(readOnly = true)
    public List<OrdenCompraDTO> listar(Optional<String> estado, Optional<Long> proveedorId) {
        List<OrdenCompra> data;
        if (estado.isPresent() && !estado.get().isBlank()) {
            data = repo.findByEstado(parseEstado(estado.get()));
        } else if (proveedorId.isPresent()) {
            data = repo.findByProveedorIdOrderByFechaDescIdDesc(proveedorId.get());
        } else {
            data = repo.findAllByOrderByFechaDescIdDesc();
        }
        return data.stream().map(OrdenCompraMapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public OrdenCompraDTO obtener(Long id) {
        return repo.findById(id).map(OrdenCompraMapper::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("OC no encontrada: " + id));
    }

    @Transactional(readOnly = true)
    public OrdenCompraDTO obtenerPorCodigo(String codigo) {
        return repo.findByCodigo(codigo).map(OrdenCompraMapper::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("OC no encontrada: " + codigo));
    }

    @Transactional
    public OrdenCompraDTO crear(CrearOrdenCompraDTO dto) {
        Proveedor prov = proveedorService.referencia(dto.proveedorId());

        BigDecimal subtotal = dto.items().stream()
                .map(it -> it.precioUnit().multiply(BigDecimal.valueOf(it.cantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal igv = subtotal.multiply(IGV_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(igv).setScale(2, RoundingMode.HALF_UP);

        OrdenCompra oc = OrdenCompra.builder()
                .codigo(generarCodigo())
                .fecha(LocalDate.now())
                .proveedorId(prov.getId())
                .proveedorNombre(prov.getNombreComercial() != null ? prov.getNombreComercial() : prov.getRazonSocial())
                .estado(EstadoOC.borrador)
                .fechaEsperada(dto.fechaEsperada())
                .subtotal(subtotal)
                .igv(igv)
                .total(total)
                .usuario(dto.usuario())
                .notas(dto.notas())
                .build();

        for (OCItemDTO it : dto.items()) {
            oc.getItems().add(OrdenCompraMapper.itemFromDTO(oc, it));
        }
        return OrdenCompraMapper.toDTO(repo.save(oc));
    }

    @Transactional
    public OrdenCompraDTO enviar(Long id) {
        OrdenCompra oc = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("OC no encontrada: " + id));
        if (oc.getEstado() != EstadoOC.borrador) {
            throw new IllegalStateException("Solo se puede enviar una OC en borrador");
        }
        oc.setEstado(EstadoOC.enviada);
        return OrdenCompraMapper.toDTO(oc);
    }

    @Transactional
    public OrdenCompraDTO recibir(Long id, String usuario) {
        OrdenCompra oc = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("OC no encontrada: " + id));
        if (oc.getEstado() == EstadoOC.recibida) {
            return OrdenCompraMapper.toDTO(oc);
        }
        if (oc.getEstado() == EstadoOC.cancelada) {
            throw new IllegalStateException("OC cancelada no puede recibirse");
        }

        oc.setEstado(EstadoOC.recibida);
        oc.setFechaRecepcion(LocalDate.now());

        // Por cada item: registrar movimiento de entrada + ajustar stock en catalog
        for (OrdenCompraItem it : oc.getItems()) {
            Movimiento mov = Movimiento.builder()
                    .fecha(LocalDate.now())
                    .tipo(TipoMovimiento.entrada)
                    .productoId(it.getProductoId())
                    .cantidad(it.getCantidad())
                    .motivo("Compra a proveedor")
                    .referencia(oc.getCodigo())
                    .usuario(usuario != null ? usuario : oc.getUsuario())
                    .build();
            movRepo.save(mov);

            catalogClient.actualizarStock(it.getProductoId(), Map.of("delta", it.getCantidad()));
        }
        return OrdenCompraMapper.toDTO(oc);
    }

    @Transactional
    public OrdenCompraDTO cancelar(Long id) {
        OrdenCompra oc = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("OC no encontrada: " + id));
        if (oc.getEstado() == EstadoOC.recibida) {
            throw new IllegalStateException("OC ya fue recibida, no puede cancelarse");
        }
        oc.setEstado(EstadoOC.cancelada);
        return OrdenCompraMapper.toDTO(oc);
    }

    @Transactional
    public void eliminar(Long id) {
        OrdenCompra oc = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("OC no encontrada: " + id));
        if (oc.getEstado() == EstadoOC.recibida) {
            throw new IllegalStateException("OC recibida no puede eliminarse");
        }
        repo.delete(oc);
    }

    private EstadoOC parseEstado(String s) {
        try {
            return EstadoOC.valueOf(s.toLowerCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Estado de OC invalido: " + s);
        }
    }

    String generarCodigo() {
        int year = LocalDate.now().getYear();
        String prefijo = "OC-" + year + "-";
        int siguiente = repo.findByCodigoStartingWithOrderByCodigoDesc(prefijo).stream()
                .findFirst()
                .map(o -> {
                    String[] parts = o.getCodigo().split("-");
                    try { return Integer.parseInt(parts[2]) + 1; }
                    catch (Exception e) { return 1; }
                })
                .orElse(1);
        return prefijo + String.format("%04d", siguiente);
    }
}
