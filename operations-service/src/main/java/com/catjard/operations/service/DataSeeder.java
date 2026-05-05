package com.catjard.operations.service;

import com.catjard.operations.model.Arte;
import com.catjard.operations.model.Despacho;
import com.catjard.operations.model.EstadoArte;
import com.catjard.operations.model.HitoTracking;
import com.catjard.operations.model.TrackingEvento;
import com.catjard.operations.repository.ArteRepository;
import com.catjard.operations.repository.DespachoRepository;
import com.catjard.operations.repository.TrackingEventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final ArteRepository arteRepo;
    private final TrackingEventoRepository trackRepo;
    private final DespachoRepository despachoRepo;

    @Override
    public void run(String... args) {
        if (arteRepo.count() == 0) seedArtes();
        if (trackRepo.count() == 0) seedTracking();
        if (despachoRepo.count() == 0) seedDespachos();
    }

    private void seedArtes() {
        List<Arte> data = List.of(
                Arte.builder()
                        .pedidoCodigo("PED-2026-0089").version(1)
                        .nombreArchivo("arte-banco-sigma-v1.pdf")
                        .fecha(LocalDate.of(2026, 4, 19))
                        .estado(EstadoArte.rechazado)
                        .comentariosCliente("Bajar el logo 5 mm. Cambiar el color del contorno a Pantone 7551 C.")
                        .build(),
                Arte.builder()
                        .pedidoCodigo("PED-2026-0089").version(2)
                        .nombreArchivo("arte-banco-sigma-v2.pdf")
                        .fecha(LocalDate.of(2026, 4, 21))
                        .estado(EstadoArte.aprobado)
                        .comentariosCliente("Perfecto, gracias!")
                        .build(),
                Arte.builder()
                        .pedidoCodigo("PED-2026-0072").version(1)
                        .nombreArchivo("arte-tote-bcs-v1.pdf")
                        .fecha(LocalDate.of(2026, 3, 7))
                        .estado(EstadoArte.aprobado)
                        .comentariosCliente("Aprobado.")
                        .build()
        );
        arteRepo.saveAll(data);
    }

    private void seedTracking() {
        List<TrackingEvento> p89 = List.of(
                track("PED-2026-0089", HitoTracking.cotizacion_aprobada, LocalDate.of(2026, 4, 15), true),
                track("PED-2026-0089", HitoTracking.en_diseno,           LocalDate.of(2026, 4, 16), true),
                track("PED-2026-0089", HitoTracking.arte_aprobado,       LocalDate.of(2026, 4, 21), true),
                track("PED-2026-0089", HitoTracking.en_produccion,       LocalDate.of(2026, 4, 23), true),
                track("PED-2026-0089", HitoTracking.control_calidad,     null,                       false),
                track("PED-2026-0089", HitoTracking.listo,               null,                       false),
                track("PED-2026-0089", HitoTracking.despachado,          null,                       false),
                track("PED-2026-0089", HitoTracking.entregado,           null,                       false)
        );
        List<TrackingEvento> p72 = List.of(
                track("PED-2026-0072", HitoTracking.cotizacion_aprobada, LocalDate.of(2026, 3, 4),  true),
                track("PED-2026-0072", HitoTracking.en_diseno,           LocalDate.of(2026, 3, 5),  true),
                track("PED-2026-0072", HitoTracking.arte_aprobado,       LocalDate.of(2026, 3, 7),  true),
                track("PED-2026-0072", HitoTracking.en_produccion,       LocalDate.of(2026, 3, 8),  true),
                track("PED-2026-0072", HitoTracking.control_calidad,     LocalDate.of(2026, 3, 18), true),
                track("PED-2026-0072", HitoTracking.listo,               LocalDate.of(2026, 3, 19), true),
                track("PED-2026-0072", HitoTracking.despachado,          LocalDate.of(2026, 3, 20), true),
                track("PED-2026-0072", HitoTracking.entregado,           LocalDate.of(2026, 3, 22), true)
        );
        trackRepo.saveAll(p89);
        trackRepo.saveAll(p72);
    }

    private void seedDespachos() {
        Despacho d = Despacho.builder()
                .pedidoCodigo("PED-2026-0072")
                .courier("Olva Courier")
                .guiaRemision("GR-2026-001182")
                .fechaDespacho(LocalDate.of(2026, 3, 20))
                .fechaEntregaReal(LocalDate.of(2026, 3, 22))
                .direccionEntrega("Av. Javier Prado 1234, San Isidro, Lima")
                .receptor("Recepcion Banco Sigma")
                .build();
        despachoRepo.save(d);
    }

    private TrackingEvento track(String codigo, HitoTracking hito, LocalDate fecha, boolean completo) {
        return TrackingEvento.builder()
                .pedidoCodigo(codigo).hito(hito).fecha(fecha).completo(completo)
                .build();
    }
}
