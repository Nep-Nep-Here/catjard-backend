package com.catjard.inventory.service;

import com.catjard.inventory.model.*;
import com.catjard.inventory.repository.MovimientoRepository;
import com.catjard.inventory.repository.OrdenCompraRepository;
import com.catjard.inventory.repository.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final ProveedorRepository provRepo;
    private final OrdenCompraRepository ocRepo;
    private final MovimientoRepository movRepo;

    @Override
    public void run(String... args) {
        if (provRepo.count() == 0) seedProveedores();
        if (ocRepo.count() == 0) seedOC();
        if (movRepo.count() == 0) seedMovimientos();
    }

    private void seedProveedores() {
        List<Proveedor> ps = List.of(
                prov("Textiles Andinos S.A.", "Textiles Andinos", "20100123456",
                        "Juan Perez", "ventas@textilesandinos.pe", "+51 1 555 1234",
                        "Av. Industrial 234, Lima",
                        "Polos, hoodies, casacas, gorras",
                        "Proveedor principal de prendas de algodon. Buen plazo (10 dias).",
                        true, LocalDate.of(2024, 1, 15)),
                prov("Importaciones Promo S.A.C.", "Promo Imports", "20498765432",
                        "Carla Vega", "carla@promoimports.com.pe", "+51 1 555 7777",
                        "Calle Los Cedros 89, San Borja, Lima",
                        "USB, power banks, mousepads, llaveros",
                        "Tecnologia y articulos promocionales. Importa de China (45 dias).",
                        true, LocalDate.of(2024, 3, 20)),
                prov("Ceramicas Lima S.R.L.", "Ceramicas Lima", "20611223344",
                        "Roberto Quispe", "rquispe@ceramicaslima.pe", "+51 1 444 8899",
                        "Av. Argentina 5670, Callao",
                        "Tazas, tomatodos, vasos ceramicos",
                        "Local. Plazo corto. Buen acabado.",
                        true, LocalDate.of(2024, 5, 8)),
                prov("Distribuidora Papelera Norte E.I.R.L.", "Papelera Norte", "20330987654",
                        "Maria Sosa", "maria.sosa@papelnorte.pe", "+51 1 222 5566",
                        "Jr. Ucayali 1234, Lima",
                        "Libretas, agendas, lapiceros, stickers",
                        "Papeleria corporativa.",
                        true, LocalDate.of(2024, 7, 12)),
                prov("Bolsos & Accesorios del Sur S.A.", "Bolsos del Sur", "20770456123",
                        "Ricardo Huaman", "rhuaman@bolsosdelsur.com", "+51 1 333 1111",
                        "Av. Caminos del Inca 850, Surco, Lima",
                        "Mochilas, tote bags, cooler bags, paraguas", null,
                        false, LocalDate.of(2024, 9, 5))
        );
        provRepo.saveAll(ps);
    }

    private void seedOC() {
        OrdenCompra oc1 = OrdenCompra.builder()
                .codigo("OC-2026-0012").fecha(LocalDate.of(2026, 4, 10))
                .proveedorId(4L).proveedorNombre("Papelera Norte")
                .estado(EstadoOC.recibida)
                .fechaEsperada(LocalDate.of(2026, 4, 20))
                .fechaRecepcion(LocalDate.of(2026, 4, 19))
                .subtotal(new BigDecimal("10290.00")).igv(new BigDecimal("1852.20")).total(new BigDecimal("12142.20"))
                .usuario("Marta Salinas").notas("Material para pedido Banco Sigma.")
                .build();
        oc1.getItems().add(itemOC(oc1, 8L, 500, "14.50"));
        oc1.getItems().add(itemOC(oc1, 7L, 800, "3.80"));

        OrdenCompra oc2 = OrdenCompra.builder()
                .codigo("OC-2026-0014").fecha(LocalDate.of(2026, 4, 22))
                .proveedorId(1L).proveedorNombre("Textiles Andinos")
                .estado(EstadoOC.recibida)
                .fechaEsperada(LocalDate.of(2026, 4, 26))
                .fechaRecepcion(LocalDate.of(2026, 4, 25))
                .subtotal(new BigDecimal("10800.00")).igv(new BigDecimal("1944.00")).total(new BigDecimal("12744.00"))
                .usuario("Marta Salinas")
                .build();
        oc2.getItems().add(itemOC(oc2, 1L, 600, "18.00"));

        OrdenCompra oc3 = OrdenCompra.builder()
                .codigo("OC-2026-0017").fecha(LocalDate.of(2026, 4, 28))
                .proveedorId(3L).proveedorNombre("Ceramicas Lima")
                .estado(EstadoOC.enviada)
                .fechaEsperada(LocalDate.of(2026, 5, 8))
                .subtotal(new BigDecimal("7200.00")).igv(new BigDecimal("1296.00")).total(new BigDecimal("8496.00"))
                .usuario("Marta Salinas").notas("Pendiente de recepcion.")
                .build();
        oc3.getItems().add(itemOC(oc3, 3L, 400, "8.50"));
        oc3.getItems().add(itemOC(oc3, 4L, 200, "19.00"));

        ocRepo.saveAll(List.of(oc1, oc2, oc3));
    }

    private void seedMovimientos() {
        List<Movimiento> ms = List.of(
                mov(LocalDate.of(2026, 4, 19), TipoMovimiento.entrada, 8L, 500, "Compra a proveedor", "OC-2026-0012", null),
                mov(LocalDate.of(2026, 4, 19), TipoMovimiento.entrada, 7L, 800, "Compra a proveedor", "OC-2026-0012", null),
                mov(LocalDate.of(2026, 4, 23), TipoMovimiento.salida,  8L, 300, "Salida a produccion", "PED-2026-0089", "Pedido Banco Sigma."),
                mov(LocalDate.of(2026, 4, 23), TipoMovimiento.salida,  7L, 500, "Salida a produccion", "PED-2026-0089", "Pedido Banco Sigma."),
                mov(LocalDate.of(2026, 4, 25), TipoMovimiento.entrada, 1L, 600, "Compra a proveedor", "OC-2026-0014", null),
                mov(LocalDate.of(2026, 4, 28), TipoMovimiento.ajuste, 14L, -50, "Ajuste negativo", null, "Diferencia inventario fisico.")
        );
        movRepo.saveAll(ms);
    }

    private Proveedor prov(String razon, String nomCom, String ruc, String contacto, String email, String tel,
                           String direccion, String productos, String notas, boolean activo, LocalDate alta) {
        return Proveedor.builder()
                .razonSocial(razon).nombreComercial(nomCom).ruc(ruc)
                .contacto(contacto).email(email).telefono(tel).direccion(direccion)
                .productos(productos).notas(notas)
                .activo(activo).fechaAlta(alta)
                .build();
    }

    private OrdenCompraItem itemOC(OrdenCompra oc, Long prodId, int cant, String precio) {
        return OrdenCompraItem.builder()
                .ordenCompra(oc).productoId(prodId).cantidad(cant)
                .precioUnit(new BigDecimal(precio))
                .build();
    }

    private Movimiento mov(LocalDate fecha, TipoMovimiento tipo, Long prodId, int cant,
                           String motivo, String referencia, String notas) {
        return Movimiento.builder()
                .fecha(fecha).tipo(tipo).productoId(prodId).cantidad(cant)
                .motivo(motivo).referencia(referencia)
                .usuario("Marta Salinas").notas(notas)
                .build();
    }
}
