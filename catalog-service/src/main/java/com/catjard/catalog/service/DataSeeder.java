package com.catjard.catalog.service;

import com.catjard.catalog.model.AplicaA;
import com.catjard.catalog.model.Categoria;
import com.catjard.catalog.model.Producto;
import com.catjard.catalog.model.Promocion;
import com.catjard.catalog.repository.ProductoRepository;
import com.catjard.catalog.repository.PromocionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final ProductoRepository productos;
    private final PromocionRepository promociones;

    @Override
    public void run(String... args) {
        if (productos.count() == 0) seedProductos();
        if (promociones.count() == 0) seedPromociones();
    }

    private void seedProductos() {
        log.info("DataSeeder: sembrando 18 productos...");
        var seeds = List.of(
                p("polo-clasico",         "Polo clasico algodon",       Categoria.vestimenta, 28,  1200, 200,  "Polo unisex en algodon pima 180 g. Cuello redondo. Disponible en 8 colores.", Set.of("Serigrafía","DTF","Bordado","Sublimado")),
                p("polo-pique",           "Polo pique premium",         Categoria.vestimenta, 45,  600,  150,  "Polo pique con cuello tejido. Ideal para uniforme corporativo.",              Set.of("Bordado","Serigrafía")),
                p("taza-ceramica",        "Taza ceramica 11 oz",        Categoria.bebidas,    14,  800,  200,  "Taza ceramica blanca apta para sublimado full-color. Resistente al lavavajillas.", Set.of("Sublimado","Impresión UV")),
                p("tomatodo-acero",       "Tomatodo acero 600 ml",      Categoria.bebidas,    32,  350,  100,  "Termo de acero inoxidable doble pared. Mantiene 12 h frio / 6 h caliente.",   Set.of("Grabado Láser","Impresión UV")),
                p("tote-canvas",          "Tote bag canvas",            Categoria.bolsos,     18,  900,  200,  "Bolsa de canvas natural, asas largas, capacidad 8 L.",                         Set.of("Serigrafía","DTF")),
                p("mochila-corporativa",  "Mochila corporativa",        Categoria.bolsos,     78,  220,  80,   "Mochila con compartimento laptop 15\", puerto USB y bolsillo antirrobo.",     Set.of("Bordado","DTF")),
                p("lapicero-metal",       "Lapicero metalico",          Categoria.oficina,    6,   5000, 1000, "Cuerpo metalico con clip. Tinta azul, 0.7 mm.",                               Set.of("Grabado Láser")),
                p("libreta-a5",           "Libreta A5 tapa dura",       Categoria.oficina,    22,  700,  150,  "Libreta A5 con 120 hojas, tapa dura cartone, elastico.",                       Set.of("Serigrafía","Grabado Láser","Impresión UV")),
                p("agenda-2026",          "Agenda ejecutiva 2026",      Categoria.oficina,    45,  280,  80,   "Agenda anual con planificador semanal y mensual.",                             Set.of("Grabado Láser","Bordado")),
                p("usb-16gb",             "USB 16 GB metalico",         Categoria.tecnologia, 24,  450,  100,  "USB 3.0 con carcasa metalica. 16 GB.",                                         Set.of("Grabado Láser","Impresión UV")),
                p("mousepad-tela",        "Mousepad de tela",           Categoria.tecnologia, 16,  600,  150,  "Mousepad de tela 22 x 18 cm, base antideslizante.",                            Set.of("Sublimado")),
                p("gorra-bordada",        "Gorra estructurada",         Categoria.vestimenta, 32,  480,  100,  "Gorra 6 paneles con cierre ajustable. 6 colores.",                             Set.of("Bordado")),
                p("hoodie-canguro",       "Hoodie canguro premium",     Categoria.vestimenta, 88,  180,  60,   "Hoodie unisex en algodon frances 320 g.",                                       Set.of("DTF","Bordado","Serigrafía")),
                p("sticker-pack",         "Stickers vinilo (set 10)",   Categoria.oficina,    12,  2000, 500,  "Pack de 10 stickers de vinilo troquelados, resistentes al agua.",              Set.of("Impresión UV")),
                p("llavero-metal",        "Llavero metalico",           Categoria.oficina,    8,   1500, 300,  "Llavero metalico con anilla reforzada.",                                       Set.of("Grabado Láser","Impresión UV")),
                p("powerbank-10000",      "Power bank 10 000 mAh",      Categoria.tecnologia, 65,  200,  60,   "Bateria externa 10 000 mAh con doble salida USB y entrada tipo C.",            Set.of("Impresión UV","Grabado Láser")),
                p("paraguas-corporativo", "Paraguas corporativo",       Categoria.bolsos,     38,  320,  80,   "Paraguas automatico 8 varillas, tela poliester antiviento.",                   Set.of("Sublimado","Serigrafía")),
                p("cooler-bag",           "Cooler bag isotermico",      Categoria.bolsos,     42,  240,  60,   "Cooler bag 12 L, mantiene temperatura 6 h.",                                    Set.of("Serigrafía","DTF"))
        );
        productos.saveAll(seeds);
        log.info("DataSeeder: {} productos creados.", seeds.size());
    }

    private void seedPromociones() {
        log.info("DataSeeder: sembrando 3 promociones...");
        var seeds = List.of(
                Promocion.builder()
                        .nombre("Aniversario Cat Jard")
                        .descripcion("15 % off en todo el catalogo durante mayo.")
                        .descuentoPct(BigDecimal.valueOf(15))
                        .desde(LocalDate.of(2026, 5, 1))
                        .hasta(LocalDate.of(2026, 5, 31))
                        .aplicaA(AplicaA.todo)
                        .activa(true)
                        .build(),
                Promocion.builder()
                        .nombre("Vestimenta corporativa Q3")
                        .descripcion("Descuento extra en polos, hoodies y gorras.")
                        .descuentoPct(BigDecimal.valueOf(10))
                        .desde(LocalDate.of(2026, 7, 1))
                        .hasta(LocalDate.of(2026, 9, 30))
                        .aplicaA(AplicaA.categoria)
                        .categoriaAplica(Categoria.vestimenta)
                        .activa(false)
                        .build(),
                Promocion.builder()
                        .nombre("Lapicero corporativo")
                        .descripcion("Descuento al producto estrella de oficina.")
                        .descuentoPct(BigDecimal.valueOf(20))
                        .desde(LocalDate.of(2026, 4, 1))
                        .hasta(LocalDate.of(2026, 4, 30))
                        .aplicaA(AplicaA.producto)
                        .productoId(7L)
                        .activa(false)
                        .build()
        );
        promociones.saveAll(seeds);
        log.info("DataSeeder: {} promociones creadas.", seeds.size());
    }

    private Producto p(String slug, String nombre, Categoria cat, double precio, int stock, int min, String desc, Set<String> tecnicas) {
        return Producto.builder()
                .slug(slug)
                .nombre(nombre)
                .categoria(cat)
                .precio(BigDecimal.valueOf(precio))
                .stock(stock)
                .stockMinimo(min)
                .descripcion(desc)
                .tecnicas(new java.util.HashSet<>(tecnicas))
                .build();
    }
}
