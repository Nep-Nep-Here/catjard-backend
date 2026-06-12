package com.catjard.solicitudes.service;

import com.catjard.solicitudes.model.*;
import com.catjard.solicitudes.repository.CambioRepository;
import com.catjard.solicitudes.repository.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

// Siembra datos de ejemplo SOLO si las tablas estan vacias (no pisa datos existentes).
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final SolicitudRepository repo;
    private final CambioRepository cambioRepo;

    @Override
    public void run(String... args) {
        seedSolicitudes();
        seedCambios();
    }

    private void seedSolicitudes() {
        if (repo.count() > 0) return;
        log.info("DataSeeder: sembrando solicitudes de ejemplo...");

        int year = LocalDate.now().getYear();
        repo.saveAll(List.of(
                Solicitud.builder()
                        .codigo("SOL-" + year + "-0001").fecha(LocalDate.now())
                        .tipo(TipoSolicitud.acceso).prioridad(Prioridad.alta).estado(EstadoSolicitud.por_hacer)
                        .asunto("No puedo iniciar sesion")
                        .descripcion("Olvide mi contrasena y necesito restablecer el acceso a mi cuenta.")
                        .solicitanteEmail("cliente@demo.com").solicitanteNombre("Cliente Demo")
                        .build(),
                Solicitud.builder()
                        .codigo("SOL-" + year + "-0002").fecha(LocalDate.now())
                        .tipo(TipoSolicitud.informacion).prioridad(Prioridad.media).estado(EstadoSolicitud.en_curso)
                        .asunto("Consulta sobre tecnicas de estampado")
                        .descripcion("Quisiera saber que tecnicas aplican para polos en pedidos de 200 unidades.")
                        .solicitanteEmail("vendedor@demo.com").solicitanteNombre("Vendedor Demo")
                        .build(),
                Solicitud.builder()
                        .codigo("SOL-" + year + "-0003").fecha(LocalDate.now())
                        .tipo(TipoSolicitud.servicio).prioridad(Prioridad.media).estado(EstadoSolicitud.finalizado)
                        .asunto("Error al subir el voucher de pago")
                        .descripcion("El sistema no me deja adjuntar el voucher en mi pedido PED-0007.")
                        .solicitanteEmail("cliente@demo.com").solicitanteNombre("Cliente Demo")
                        .build()
        ));
        log.info("DataSeeder: 3 solicitudes de ejemplo creadas.");
    }

    private void seedCambios() {
        if (cambioRepo.count() > 0) return;
        log.info("DataSeeder: sembrando cambios de ejemplo...");

        int year = LocalDate.now().getYear();
        cambioRepo.saveAll(List.of(
                Cambio.builder()
                        .codigo("CHN-" + year + "-001").fecha(LocalDate.now())
                        .proyecto("Plataforma Cat Jard").version("1.0")
                        .tipoCambio(TipoCambio.normal).categoria(CategoriaCambio.aplicaciones)
                        .titulo("Despliegue del microservicio de Solicitudes + Jira")
                        .descripcion("Alta de la mesa de ayuda (tickets) e integracion con Jira para gestion de cambios.")
                        .impacto(Nivel.medio).prioridad(Prioridad.alta)
                        .estado(EstadoCambio.aprobado).etapaCI(EtapaCI.testing)
                        .responsable("Equipo Backend").solicitanteEmail("gerente@demo.com")
                        .areaAfectada("Aplicaciones y BD").riesgo("Falla en la migracion Flyway.").nivelRiesgo(Nivel.medio)
                        .planPruebas("Unitarias (JUnit), build Maven/Vite, humo funcional.")
                        .planRollback("docker compose stop solicitudes-service; restaurar BD desde pg_dump.")
                        .build(),
                Cambio.builder()
                        .codigo("CHE-" + year + "-001").fecha(LocalDate.now())
                        .proyecto("Infraestructura").version("1.0")
                        .tipoCambio(TipoCambio.estandar).categoria(CategoriaCambio.infraestructura)
                        .titulo("Respaldo diario de la base de datos (pg_dump)")
                        .descripcion("Tarea programada de respaldo logico de las 7 bases de datos.")
                        .impacto(Nivel.bajo).prioridad(Prioridad.media)
                        .estado(EstadoCambio.implementado).etapaCI(EtapaCI.monitoreo)
                        .responsable("DevOps").solicitanteEmail("gerente@demo.com")
                        .areaAfectada("Infraestructura").riesgo("Consumo de disco.").nivelRiesgo(Nivel.bajo)
                        .build(),
                Cambio.builder()
                        .codigo("CHM-" + year + "-001").fecha(LocalDate.now())
                        .proyecto("Seguridad").version("1.0")
                        .tipoCambio(TipoCambio.emergencia).categoria(CategoriaCambio.aplicaciones)
                        .titulo("Hotfix de rate-limit en el login")
                        .descripcion("Correccion critica para mitigar intentos de fuerza bruta en el login.")
                        .impacto(Nivel.alto).prioridad(Prioridad.alta)
                        .estado(EstadoCambio.solicitado).etapaCI(EtapaCI.pendiente)
                        .responsable("Seguridad").solicitanteEmail("gerente@demo.com")
                        .areaAfectada("Identity-service").riesgo("Bloqueo de usuarios legitimos.").nivelRiesgo(Nivel.medio)
                        .build()
        ));
        log.info("DataSeeder: 3 cambios de ejemplo creados.");
    }
}
