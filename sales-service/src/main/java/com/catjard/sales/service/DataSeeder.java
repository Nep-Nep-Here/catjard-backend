package com.catjard.sales.service;

import com.catjard.sales.model.*;
import com.catjard.sales.repository.CotizacionRepository;
import com.catjard.sales.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CotizacionRepository cotRepo;
    private final PedidoRepository pedRepo;

    @Override
    public void run(String... args) {
        if (cotRepo.count() == 0) seedCotizaciones();
        if (pedRepo.count() == 0) seedPedidos();
    }

    private void seedCotizaciones() {
        // COT-2026-0040 (aprobada, vinculada a PED-2026-0089)
        Cotizacion c1 = Cotizacion.builder()
                .codigo("COT-2026-0040").clienteId(1L).empresa("Banco Sigma").ruc("20512345671")
                .fecha(LocalDate.of(2026, 4, 15))
                .logoNombre("logo-banco-sigma.png")
                .notasCliente("Para entrega de fin de anio a colaboradores.")
                .estado(EstadoCotizacion.aprobada)
                .subtotal(new BigDecimal("7950.00")).igv(new BigDecimal("1431.00")).total(new BigDecimal("9381.00"))
                .validez(LocalDate.of(2026, 5, 15))
                .notasVendedor("Incluye diseno, una ronda de cambios y plotter de muestra. Plazo: 12 dias habiles.")
                .vendedor("Carlos Rivas")
                .pedidoCodigo("PED-2026-0089")
                .build();
        c1.getItems().add(itemCot(c1, 8L, 300, "18.70", "Grabado Laser", null));
        c1.getItems().add(itemCot(c1, 7L, 500, "4.68",  "Grabado Laser", null));

        Cotizacion c2 = Cotizacion.builder()
                .codigo("COT-2026-0058").clienteId(1L).empresa("Banco Sigma").ruc("20512345671")
                .fecha(LocalDate.of(2026, 4, 22))
                .logoNombre("logo-banco-sigma.png")
                .notasCliente("Polos para evento de aniversario.")
                .estado(EstadoCotizacion.propuesta)
                .subtotal(new BigDecimal("4760.00")).igv(new BigDecimal("856.80")).total(new BigDecimal("5616.80"))
                .validez(LocalDate.of(2026, 5, 22))
                .notasVendedor("Producto en stock. Plazo de entrega: 8 dias habiles desde la aprobacion.")
                .vendedor("Carlos Rivas")
                .build();
        c2.getItems().add(itemCot(c2, 1L, 200, "23.80", "Bordado", "Bordado en pecho izquierdo, 8 cm."));

        Cotizacion c3 = Cotizacion.builder()
                .codigo("COT-2026-0071").clienteId(1L).empresa("Banco Sigma").ruc("20512345671")
                .fecha(LocalDate.of(2026, 4, 28))
                .notasCliente("Cantidad pequena, urgente.")
                .estado(EstadoCotizacion.rechazada)
                .subtotal(new BigDecimal("5280.00")).igv(new BigDecimal("950.40")).total(new BigDecimal("6230.40"))
                .validez(LocalDate.of(2026, 5, 28))
                .motivoRechazo("Plazo no calza con nuestra ventana de entrega.")
                .vendedor("Carlos Rivas")
                .build();
        c3.getItems().add(itemCot(c3, 13L, 60, "88.00", "DTF", null));

        Cotizacion c4 = Cotizacion.builder()
                .codigo("COT-2026-0083").clienteId(1L).empresa("Banco Sigma").ruc("20512345671")
                .fecha(LocalDate.of(2026, 5, 1))
                .logoNombre("logo-banco-sigma.png")
                .notasCliente("Para sorteo de cumpleanios del staff.")
                .estado(EstadoCotizacion.enviada)
                .subtotal(new BigDecimal("1932.00")).igv(new BigDecimal("347.76")).total(new BigDecimal("2279.76"))
                .validez(LocalDate.of(2026, 6, 1))
                .build();
        c4.getItems().add(itemCot(c4, 3L, 150, "12.88", "Sublimado", null));

        cotRepo.saveAll(List.of(c1, c2, c3, c4));
    }

    private void seedPedidos() {
        // PED-2026-0089 (en produccion, vinculado a COT-2026-0040)
        Pedido p1 = Pedido.builder()
                .codigo("PED-2026-0089").cotizacionCodigo("COT-2026-0040")
                .clienteId(1L).empresa("Banco Sigma")
                .fechaPedido(LocalDate.of(2026, 4, 16))
                .fechaEntregaEstimada(LocalDate.of(2026, 5, 8))
                .subtotal(new BigDecimal("7950.00")).igv(new BigDecimal("1431.00")).total(new BigDecimal("9381.00"))
                .voucherUrl("voucher-bcs-040.pdf").voucherFecha(LocalDate.of(2026, 4, 17))
                .estado(EstadoPedido.en_produccion)
                .procesadoPor("Carlos Rivas")
                .build();
        p1.getItems().add(itemPed(p1, 8L, 300, "18.70", "Grabado Laser"));
        p1.getItems().add(itemPed(p1, 7L, 500, "4.68",  "Grabado Laser"));

        Pedido p2 = Pedido.builder()
                .codigo("PED-2026-0072").cotizacionCodigo("COT-2026-0021")
                .clienteId(1L).empresa("Banco Sigma")
                .fechaPedido(LocalDate.of(2026, 3, 4))
                .fechaEntregaEstimada(LocalDate.of(2026, 3, 22))
                .subtotal(new BigDecimal("5616.00")).igv(new BigDecimal("1010.88")).total(new BigDecimal("6626.88"))
                .voucherUrl("voucher-bcs-021.pdf").voucherFecha(LocalDate.of(2026, 3, 5))
                .estado(EstadoPedido.entregado)
                .courier("Olva Courier").guiaRemision("GR-2026-001182")
                .procesadoPor("Carlos Rivas")
                .build();
        p2.getItems().add(itemPed(p2, 5L, 400, "14.04", "Serigrafia"));

        pedRepo.saveAll(List.of(p1, p2));
    }

    private CotizacionItem itemCot(Cotizacion c, Long prodId, int cant, String precio, String tec, String notas) {
        return CotizacionItem.builder()
                .cotizacion(c).productoId(prodId).cantidad(cant)
                .precioUnit(new BigDecimal(precio)).tecnica(tec).notas(notas)
                .build();
    }

    private PedidoItem itemPed(Pedido p, Long prodId, int cant, String precio, String tec) {
        return PedidoItem.builder()
                .pedido(p).productoId(prodId).cantidad(cant)
                .precioUnit(new BigDecimal(precio)).tecnica(tec)
                .build();
    }
}
