package com.catjard.solicitudes.service;

import com.catjard.solicitudes.model.*;
import com.catjard.solicitudes.repository.ArticuloKBRepository;
import com.catjard.solicitudes.repository.CambioRepository;
import com.catjard.solicitudes.repository.IncidenteRepository;
import com.catjard.solicitudes.repository.RespaldoRepository;
import com.catjard.solicitudes.repository.RiesgoRepository;
import com.catjard.solicitudes.repository.ServicioCriticoRepository;
import com.catjard.solicitudes.repository.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

// Siembra datos de ejemplo SOLO si las tablas estan vacias (no pisa datos existentes).
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final SolicitudRepository repo;
    private final CambioRepository cambioRepo;
    private final IncidenteRepository incidenteRepo;
    private final ServicioCriticoRepository servicioRepo;
    private final RiesgoRepository riesgoRepo;
    private final RespaldoRepository respaldoRepo;
    private final ArticuloKBRepository kbRepo;

    @Override
    public void run(String... args) {
        seedSolicitudes();
        seedCambios();
        seedIncidentes();
        seedContinuidad();
        seedBaseConocimiento();
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

    private void seedIncidentes() {
        if (incidenteRepo.count() > 0) return;
        log.info("DataSeeder: sembrando incidentes de ejemplo...");

        int year = LocalDate.now().getYear();
        incidenteRepo.saveAll(List.of(
                // Ejemplo del docente: impacto alto + urgencia alta => prioridad CRITICA.
                Incidente.builder()
                        .codigo("INC-" + year + "-001").fecha(LocalDate.now())
                        .titulo("Portal institucional caido")
                        .descripcion("El portal institucional no responde; los usuarios no pueden acceder.")
                        .origen(OrigenIncidente.monitoreo).servicioAfectado("Portal institucional")
                        .categoria(CategoriaIncidente.aplicaciones)
                        .impacto(Nivel.alto).urgencia(Nivel.alto).prioridad(PrioridadIncidente.critica)
                        .estado(EstadoIncidente.en_resolucion)
                        .responsable("Equipo Backend").solicitanteEmail("gerente@demo.com")
                        .diagnostico("Saturacion de conexiones a la BD detras del API Gateway.")
                        .build(),
                // Ejemplo del docente: impacto bajo + urgencia baja => prioridad BAJA.
                Incidente.builder()
                        .codigo("INC-" + year + "-002").fecha(LocalDate.now())
                        .titulo("Impresora de oficina no funciona")
                        .descripcion("La impresora de la oficina de ventas no imprime.")
                        .origen(OrigenIncidente.usuario).servicioAfectado("Impresora de oficina")
                        .categoria(CategoriaIncidente.infraestructura)
                        .impacto(Nivel.bajo).urgencia(Nivel.bajo).prioridad(PrioridadIncidente.baja)
                        .estado(EstadoIncidente.registrado)
                        .responsable("Soporte TI").solicitanteEmail("vendedor@demo.com")
                        .build(),
                // impacto alto + urgencia media => prioridad ALTA.
                Incidente.builder()
                        .codigo("INC-" + year + "-003").fecha(LocalDate.now())
                        .titulo("Intentos de acceso no autorizado al login")
                        .descripcion("Se detecta un pico de intentos fallidos de inicio de sesion en identity-service.")
                        .origen(OrigenIncidente.monitoreo).servicioAfectado("identity-service")
                        .categoria(CategoriaIncidente.seguridad)
                        .impacto(Nivel.alto).urgencia(Nivel.medio).prioridad(PrioridadIncidente.alta)
                        .estado(EstadoIncidente.en_diagnostico)
                        .responsable("Seguridad").solicitanteEmail("gerente@demo.com")
                        .build()
        ));
        log.info("DataSeeder: 3 incidentes de ejemplo creados.");
    }

    // Gestion de Continuidad y DRP: catalogo real de Cat Jard (Fase 1/3), matriz
    // de riesgos del Droplet (Fase 2) y respaldos de ejemplo (Fase 5).
    private void seedContinuidad() {
        if (servicioRepo.count() > 0) return;
        log.info("DataSeeder: sembrando catalogo de continuidad...");

        var bd = servicioRepo.save(ServicioCritico.builder()
                .codigo("SRV-001").nombre("Base de datos PostgreSQL")
                .descripcion("Cluster unico con las 6 BDs del sistema (identity, catalog, crm, sales, inventory, operations). Volumen pgdata del Droplet.")
                .tipo(TipoServicio.base_datos).criticidad(CriticidadServicio.critica)
                .prioridadRecuperacion(1).rtoMinutos(60).rpoMinutos(1440)
                .estrategiaContinuidad("Regla 3-2-1: pg_dump diario (02:00, cron del Droplet) + snapshot semanal del Droplet en DigitalOcean + copia externa en PC del equipo. Procedimiento de restore documentado y probado (scripts/backup/RESTORE.md). Objetivo futuro: WAL archiving para PITR (RPO en minutos).")
                .activo(true).build());
        var droplet = servicioRepo.save(ServicioCritico.builder()
                .codigo("SRV-002").nombre("Infraestructura Droplet DigitalOcean")
                .descripcion("Droplet Ubuntu 24.04 (4vCPU/8GB, NYC3) que aloja los 10 contenedores Docker del sistema. Punto unico de falla actual.")
                .tipo(TipoServicio.infraestructura).criticidad(CriticidadServicio.critica)
                .prioridadRecuperacion(2).rtoMinutos(120).rpoMinutos(10080)
                .estrategiaContinuidad("Snapshot semanal en DigitalOcean (restaurable en otra region), firewall solo 22/80, swap 2GB, restart unless-stopped por contenedor y monitoreo de metricas con auto-creacion de incidentes. Arquitectura HA objetivo (documentada, no implementada por costo): 2 Droplets + Load Balancer + Managed PostgreSQL.")
                .activo(true).build());
        var identity = servicioRepo.save(ServicioCritico.builder()
                .codigo("SRV-003").nombre("Identity Service (autenticacion)")
                .descripcion("Emision y validacion de JWT. Sin este servicio ningun usuario puede iniciar sesion en el sistema.")
                .tipo(TipoServicio.microservicio).criticidad(CriticidadServicio.critica)
                .prioridadRecuperacion(3).rtoMinutos(30).rpoMinutos(null)
                .estrategiaContinuidad("Contenedor sin estado (datos en la BD): se recupera con docker compose up. Imagen reconstruible desde GitHub.")
                .activo(true).build());
        var gateway = servicioRepo.save(ServicioCritico.builder()
                .codigo("SRV-004").nombre("API Gateway")
                .descripcion("Punto unico de entrada de la API (:8080 via nginx). Si cae, el frontend queda sin backend.")
                .tipo(TipoServicio.microservicio).criticidad(CriticidadServicio.critica)
                .prioridadRecuperacion(4).rtoMinutos(30).rpoMinutos(null)
                .estrategiaContinuidad("Sin estado; registro en Eureka al arrancar. Recuperacion via docker compose up.")
                .activo(true).build());
        var portal = servicioRepo.save(ServicioCritico.builder()
                .codigo("SRV-005").nombre("Portal web Cat Jard (frontend)")
                .descripcion("SPA React servida por nginx en el puerto 80; hace reverse-proxy de /api al gateway.")
                .tipo(TipoServicio.frontend).criticidad(CriticidadServicio.alta)
                .prioridadRecuperacion(5).rtoMinutos(60).rpoMinutos(null)
                .estrategiaContinuidad("Codigo respaldado en GitHub; build reproducible en el compose. Sin datos propios.")
                .activo(true).build());
        var solicitudes = servicioRepo.save(ServicioCritico.builder()
                .codigo("SRV-006").nombre("Solicitudes Service (mesa de ayuda + monitoreo)")
                .descripcion("Solicitudes, cambios, incidentes, eventos de monitoreo y este modulo de continuidad. Integra Jira y la API de DigitalOcean.")
                .tipo(TipoServicio.microservicio).criticidad(CriticidadServicio.alta)
                .prioridadRecuperacion(6).rtoMinutos(120).rpoMinutos(null)
                .estrategiaContinuidad("Datos en la BD (cubiertos por su plan de respaldos). La trazabilidad externa persiste en Jira (GDICJ/CDCCJ) aunque el servicio caiga.")
                .activo(true).build());
        var ventas = servicioRepo.save(ServicioCritico.builder()
                .codigo("SRV-007").nombre("Sales Service (ventas y pedidos)")
                .descripcion("Pedidos y ventas del negocio: la operacion comercial depende de el.")
                .tipo(TipoServicio.microservicio).criticidad(CriticidadServicio.alta)
                .prioridadRecuperacion(7).rtoMinutos(120).rpoMinutos(null)
                .estrategiaContinuidad("Sin estado propio; datos en la BD. Recuperacion via docker compose up.")
                .activo(true).build());
        var catalogo = servicioRepo.save(ServicioCritico.builder()
                .codigo("SRV-008").nombre("Catalog Service (catalogo de productos)")
                .tipo(TipoServicio.microservicio).criticidad(CriticidadServicio.media)
                .prioridadRecuperacion(8).rtoMinutos(240).rpoMinutos(null)
                .estrategiaContinuidad("Sin estado propio; datos en la BD.")
                .activo(true).build());
        servicioRepo.save(ServicioCritico.builder()
                .codigo("SRV-009").nombre("Inventory Service (inventario)")
                .tipo(TipoServicio.microservicio).criticidad(CriticidadServicio.media)
                .prioridadRecuperacion(9).rtoMinutos(240).rpoMinutos(null)
                .estrategiaContinuidad("Sin estado propio; datos en la BD.")
                .activo(true).build());
        servicioRepo.save(ServicioCritico.builder()
                .codigo("SRV-010").nombre("Operations / CRM Service")
                .descripcion("Operaciones (produccion, despachos, tracking) y CRM (leads).")
                .tipo(TipoServicio.microservicio).criticidad(CriticidadServicio.media)
                .prioridadRecuperacion(10).rtoMinutos(240).rpoMinutos(null)
                .estrategiaContinuidad("Sin estado propio; datos en la BD.")
                .activo(true).build());

        int year = LocalDate.now().getYear();
        riesgoRepo.saveAll(List.of(
                Riesgo.builder()
                        .codigo("RSG-" + year + "-001").nombre("Agotamiento de recursos del Droplet (RAM/disco)")
                        .descripcion("8 JVMs en 8GB: fugas de memoria, logs o imagenes Docker acumuladas pueden degradar o tumbar los servicios.")
                        .probabilidad(Nivel.alto).impacto(Nivel.medio).nivelRiesgo(NivelRiesgo.alto)
                        .accionMitigacion("Monitoreo de eventos con umbrales (auto-crea incidente en alto/critico), mem_limit por contenedor, swap 2GB y limpieza periodica de logs/imagenes.")
                        .estado(EstadoRiesgo.en_mitigacion)
                        .servicios(Set.of(droplet, bd, gateway)).build(),
                Riesgo.builder()
                        .codigo("RSG-" + year + "-002").nombre("Caida del Droplet (OOM, kernel panic, reinicio)")
                        .descripcion("Todo el sistema corre en un unico servidor: cualquier caida del host interrumpe los 10 contenedores a la vez.")
                        .probabilidad(Nivel.medio).impacto(Nivel.alto).nivelRiesgo(NivelRiesgo.alto)
                        .accionMitigacion("restart unless-stopped en los contenedores, swap de seguridad y monitoreo con alertas de DigitalOcean. Plan HA objetivo: 2 Droplets + Load Balancer.")
                        .estado(EstadoRiesgo.en_mitigacion)
                        .servicios(Set.of(droplet, identity, gateway, portal)).build(),
                Riesgo.builder()
                        .codigo("RSG-" + year + "-003").nombre("Borrado o corrupcion del volumen pgdata")
                        .descripcion("Un 'docker compose down -v', un fallo de disco o una migracion mal aplicada pueden perder las 6 bases de datos.")
                        .probabilidad(Nivel.bajo).impacto(Nivel.alto).nivelRiesgo(NivelRiesgo.medio)
                        .accionMitigacion("pg_dump diario + snapshot semanal + copia externa (regla 3-2-1); procedimiento de restore documentado y ensayado.")
                        .estado(EstadoRiesgo.en_mitigacion)
                        .servicios(Set.of(bd)).build(),
                Riesgo.builder()
                        .codigo("RSG-" + year + "-004").nombre("Ransomware o acceso no autorizado al servidor")
                        .descripcion("Compromiso del Droplet via SSH o una vulnerabilidad expuesta: cifrado o exfiltracion de datos.")
                        .probabilidad(Nivel.bajo).impacto(Nivel.alto).nivelRiesgo(NivelRiesgo.medio)
                        .accionMitigacion("Firewall DO solo 22/80, autenticacion por SSH key, tokens de API de solo lectura y snapshots fuera del Droplet (inmunes al cifrado local).")
                        .estado(EstadoRiesgo.en_mitigacion)
                        .servicios(Set.of(droplet, bd, identity)).build(),
                Riesgo.builder()
                        .codigo("RSG-" + year + "-005").nombre("Desastre en la region NYC3 de DigitalOcean")
                        .descripcion("Incendio, corte electrico o falla regional del proveedor: equivalente al 'incendio en el data center' del caso de estudio.")
                        .probabilidad(Nivel.bajo).impacto(Nivel.alto).nivelRiesgo(NivelRiesgo.medio)
                        .accionMitigacion("El snapshot semanal permite recrear el Droplet en otra region (SFO/AMS) con IP nueva; la copia externa en PC cubre el peor caso (perdida total del proveedor).")
                        .estado(EstadoRiesgo.identificado)
                        .servicios(Set.of(droplet, bd, portal)).build(),
                Riesgo.builder()
                        .codigo("RSG-" + year + "-006").nombre("Error humano en despliegue o configuracion")
                        .descripcion("Un git pull + compose up con codigo roto o un .env mal editado dejan servicios caidos.")
                        .probabilidad(Nivel.medio).impacto(Nivel.medio).nivelRiesgo(NivelRiesgo.medio)
                        .accionMitigacion("Gestion de Cambios (tablero CDCCJ) para cambios planificados; imagenes reconstruibles y datos intactos en pgdata permiten revertir con git checkout + rebuild.")
                        .estado(EstadoRiesgo.en_mitigacion)
                        .servicios(Set.of(gateway, portal, solicitudes, ventas, catalogo)).build(),
                Riesgo.builder()
                        .codigo("RSG-" + year + "-007").nombre("Expiracion de credenciales de integraciones (DO / Jira)")
                        .descripcion("El token de DigitalOcean expira ~oct " + year + " y el de Jira puede revocarse: se pierde monitoreo y trazabilidad externa.")
                        .probabilidad(Nivel.medio).impacto(Nivel.bajo).nivelRiesgo(NivelRiesgo.bajo)
                        .accionMitigacion("Renovacion calendarizada de tokens; el monitoreo degrada de forma segura (la integracion se apaga sin tumbar el servicio).")
                        .estado(EstadoRiesgo.identificado)
                        .servicios(Set.of(solicitudes)).build()
        ));

        var ahora = LocalDateTime.now();
        respaldoRepo.saveAll(List.of(
                Respaldo.builder()
                        .codigo("RSP-" + year + "-001").fechaHora(ahora.minusHours(3))
                        .servicioId(bd.getId()).recurso("6 BDs PostgreSQL (pg_dump formato custom)")
                        .tipo(TipoRespaldo.completo).destino(DestinoRespaldo.droplet_local)
                        .estado(EstadoRespaldo.exitoso).tamanoMb(85.0).duracionSeg(42)
                        .mensaje("Dump diario 02:00 (cron del Droplet). Retencion 30 dias.")
                        .origen(OrigenRespaldo.simulado).build(),
                Respaldo.builder()
                        .codigo("RSP-" + year + "-002").fechaHora(ahora.minusDays(2))
                        .servicioId(droplet.getId()).recurso("Droplet completo (imagen)")
                        .tipo(TipoRespaldo.snapshot).destino(DestinoRespaldo.snapshot_do)
                        .estado(EstadoRespaldo.exitoso).tamanoMb(14200.0).duracionSeg(310)
                        .mensaje("Snapshot semanal en DigitalOcean (restaurable en otra region).")
                        .origen(OrigenRespaldo.simulado).build(),
                Respaldo.builder()
                        .codigo("RSP-" + year + "-003").fechaHora(ahora.minusHours(20))
                        .servicioId(bd.getId()).recurso("Dumps del dia (scp a PC del equipo)")
                        .tipo(TipoRespaldo.completo).destino(DestinoRespaldo.copia_externa)
                        .estado(EstadoRespaldo.exitoso).tamanoMb(85.0).duracionSeg(65)
                        .mensaje("Copia externa de la regla 3-2-1 (fuera del proveedor).")
                        .origen(OrigenRespaldo.simulado).build()
        ));

        log.info("DataSeeder: continuidad sembrada (10 servicios, 7 riesgos, 3 respaldos).");
    }

    // Base de Conocimiento: los planes del curso como articulos generales y
    // runbooks (estrategia de recuperacion paso a paso) por escenario, vinculados
    // a la categoria de incidente y al servicio del catalogo para las sugerencias.
    private void seedBaseConocimiento() {
        if (kbRepo.count() > 0) return;
        log.info("DataSeeder: sembrando Base de Conocimiento...");

        int year = LocalDate.now().getYear();
        Long bdId = servicioPorCodigo("SRV-001");
        Long dropletId = servicioPorCodigo("SRV-002");

        kbRepo.saveAll(List.of(
                ArticuloKB.builder()
                        .codigo("KB-" + year + "-001")
                        .titulo("Plan de Continuidad del Servicio (ITSCM) — Cat Jard")
                        .categoria(CategoriaKB.continuidad_servicio)
                        .resumen("Servicios criticos de la plataforma, criticidad, prioridad de recuperacion y estrategia general de continuidad.")
                        .contenido("""
                                OBJETIVO
                                Garantizar que los servicios de Cat Jard continuen operando o se recuperen dentro de tiempos aceptables ante incidentes graves o desastres.

                                SERVICIOS CRITICOS (ver catalogo vivo en Continuidad y DRP > Servicios criticos)
                                1. Base de datos PostgreSQL (critica) — se recupera primero.
                                2. Infraestructura Droplet DigitalOcean (critica).
                                3. Identity Service / autenticacion (critica): sin el nadie inicia sesion.
                                4. API Gateway (critica): punto unico de entrada.
                                5. Portal web, Solicitudes, Ventas (alta) y el resto de microservicios (media).

                                PILARES DEL PLAN
                                - Deteccion automatica: monitoreo de metricas del Droplet con auto-creacion de incidentes.
                                - Contador RTO por incidente: cada interrupcion se mide contra su objetivo.
                                - Respaldos con regla 3-2-1 (ver KB Plan de Respaldos).
                                - Trazabilidad: evento -> incidente -> Jira (GDICJ) -> resolucion -> metricas.

                                GOBIERNO
                                El cumplimiento RTO/RPO se revisa en el tablero de Continuidad; los riesgos se
                                gestionan en la matriz de riesgos con responsable y accion de mitigacion.""")
                        .autor("Equipo TI Cat Jard").vistas(0).build(),
                ArticuloKB.builder()
                        .codigo("KB-" + year + "-002")
                        .titulo("RTO y RPO por servicio")
                        .categoria(CategoriaKB.continuidad_servicio)
                        .resumen("Tiempo objetivo de recuperacion (RTO) y perdida maxima de datos aceptable (RPO) de cada servicio de la plataforma.")
                        .contenido("""
                                DEFINICIONES
                                - RTO (Recovery Time Objective): tiempo maximo permitido para recuperar un servicio.
                                - RPO (Recovery Point Objective): cantidad maxima de informacion que puede perderse.

                                OBJETIVOS VIGENTES (fuente de verdad: Continuidad y DRP > Servicios criticos)
                                | Servicio                         | RTO    | RPO    |
                                | Base de datos PostgreSQL         | 1 h    | 24 h   |
                                | Infraestructura Droplet          | 2 h    | 7 d    |
                                | Identity Service / API Gateway   | 30 min | —      |
                                | Portal web                       | 1 h    | —      |
                                | Solicitudes / Ventas             | 2 h    | —      |
                                | Catalogo / Inventario / Ops-CRM  | 4 h    | —      |

                                COMO SE MIDE
                                Al asociar un incidente a un servicio se fija su deadline (creacion + RTO) y el
                                panel muestra el contador en vivo; al resolver queda registrado si se cumplio.
                                El RPO se vigila con el semaforo (tiempo desde el ultimo respaldo exitoso).""")
                        .autor("Equipo TI Cat Jard").vistas(0).build(),
                ArticuloKB.builder()
                        .codigo("KB-" + year + "-003")
                        .titulo("Plan de Recuperacion ante Desastres (DRP)")
                        .categoria(CategoriaKB.recuperacion_desastres)
                        .resumen("Procedimiento general de recuperacion ante un desastre: deteccion, activacion, restauracion, verificacion y post-mortem.")
                        .contenido("""
                                CUANDO SE ACTIVA
                                Perdida total o prolongada del Droplet, corrupcion de la base de datos, ransomware
                                o desastre en la region NYC3 de DigitalOcean.

                                PROCEDIMIENTO GENERAL
                                1. Deteccion: el monitoreo avisa (evento critico -> incidente automatico) o se reporta manualmente.
                                2. Registrar/confirmar el incidente y evaluar el alcance (¿un servicio o todo el Droplet?).
                                3. Activar la copia correspondiente:
                                   - Un servicio o una BD -> runbook KB Restaurar BD desde el dump diario.
                                   - Droplet completo -> runbook KB Caida total del Droplet (restaurar snapshot).
                                4. Restaurar y levantar los servicios (docker compose up -d).
                                5. Verificar: portal accesible, login funciona, pedidos visibles, panel de monitoreo en verde.
                                6. Habilitar el acceso de usuarios y monitorear la estabilidad (30-60 min).
                                7. Resolver el incidente en el panel (queda medido el cumplimiento del RTO).
                                8. Post-mortem: documentar causa raiz y actualizar este plan o el runbook usado.

                                METAS
                                RTO maximo 2 horas para el sistema completo; perdida maxima de datos 24 horas
                                (dump diario). Los resultados reales quedan en el tablero de Continuidad.""")
                        .autor("Equipo TI Cat Jard").vistas(0).build(),
                ArticuloKB.builder()
                        .codigo("KB-" + year + "-004")
                        .titulo("Plan de Respaldos y regla 3-2-1")
                        .categoria(CategoriaKB.respaldos)
                        .resumen("Que se respalda, con que frecuencia y retencion; como se aplica la regla 3-2-1 en Cat Jard y donde queda registrado.")
                        .servicioId(bdId)
                        .contenido("""
                                REGLA 3-2-1 APLICADA
                                - 3 copias: produccion + dump diario + imagen semanal del Droplet.
                                - 2 medios distintos: disco del Droplet y respaldo administrado de DigitalOcean.
                                - 1 copia fuera del proveedor: descarga de dumps a la PC del equipo (scp).

                                CALENDARIO
                                | Recurso                  | Tipo      | Frecuencia        | Retencion |
                                | 7 BDs PostgreSQL (dump)  | Completo  | Diario 02:00 Peru | 30 dias   |
                                | Droplet completo (imagen)| Snapshot  | Semanal (DO)      | 28 dias   |
                                | Copia externa (PC)       | Completo  | Semanal/a demanda | 90 dias   |

                                TRAZABILIDAD
                                Cada ejecucion queda registrada en Continuidad y DRP > Respaldos: el cron del
                                Droplet reporta via API (token X-Backup-Token), los backups de DigitalOcean se
                                sincronizan con el boton del panel, y el semaforo RPO compara el ultimo respaldo
                                exitoso contra el objetivo de cada servicio.

                                SCRIPTS
                                scripts/backup/droplet/backup-diario.sh (cron), descargar-dumps.ps1 (copia externa),
                                restaurar-bd.sh (restauracion cronometrada).""")
                        .autor("Equipo TI Cat Jard").vistas(0).build(),
                ArticuloKB.builder()
                        .codigo("KB-" + year + "-005")
                        .titulo("Runbook: restaurar la base de datos desde el dump diario")
                        .categoria(CategoriaKB.runbook)
                        .resumen("Paso a paso probado para recuperar una BD (o las 7) desde el ultimo pg_dump. El tiempo medido es el RTO real.")
                        .categoriaIncidente(CategoriaIncidente.base_datos)
                        .servicioId(bdId)
                        .contenido("""
                                ESCENARIO
                                Se corrompio o se borro una base de datos (p. ej. catjard_sales), pero el Droplet
                                sigue operativo.

                                PASOS (en el Droplet, por SSH)
                                1. Confirmar el alcance: docker exec -it catjard-postgres-1 psql -U postgres -l
                                2. Ubicar el ultimo dump: ls ~/catjard-backups/dumps/ (carpeta mas reciente).
                                3. Restaurar cronometrando (el script mide el tiempo):
                                   bash ~/catjard/backend/scripts/backup/droplet/restaurar-bd.sh catjard_sales
                                4. Reiniciar el microservicio afectado para limpiar cache/conexiones:
                                   cd ~/catjard/backend && docker compose -f docker-compose.yml -f docker-compose.prod.yml restart sales-service
                                5. Verificar en el portal que los datos volvieron (pedidos, catalogo, etc.).
                                6. Resolver el incidente en el panel: el contador RTO registra si se cumplio.
                                7. Anotar el tiempo medido en el documento del plan (resultados reales).

                                PERDIDA ESPERADA
                                Como maximo lo transcurrido desde el dump de las 02:00 (RPO 24 h).""")
                        .autor("Equipo TI Cat Jard").vistas(0).build(),
                ArticuloKB.builder()
                        .codigo("KB-" + year + "-006")
                        .titulo("Runbook: caida total del Droplet — restaurar desde snapshot")
                        .categoria(CategoriaKB.runbook)
                        .resumen("Recuperacion del servidor completo desde el backup semanal de DigitalOcean, incluso en otra region.")
                        .categoriaIncidente(CategoriaIncidente.infraestructura)
                        .servicioId(dropletId)
                        .contenido("""
                                ESCENARIO
                                El Droplet no responde, se corrompio, fue comprometido, o la region NYC3 esta caida.

                                PASOS (panel de DigitalOcean)
                                1. Intentar primero lo simple: Power Cycle desde el panel (cubre cuelgues).
                                2. Si no vuelve: Backups & Snapshots -> elegir el backup mas reciente ->
                                   Restore (mismo Droplet) o Convert to snapshot -> Create Droplet (nuevo servidor,
                                   puede ser en otra region si el desastre es regional).
                                3. Si es un Droplet nuevo: apuntar el acceso a la IP nueva y aplicar el firewall
                                   (solo puertos 22 y 80).
                                4. Dentro del servidor: cd ~/catjard/backend && docker compose -f docker-compose.yml
                                   -f docker-compose.prod.yml up -d
                                5. La BD restaurada trae datos de hasta 7 dias atras: aplicar el dump diario mas
                                   reciente disponible (runbook KB de restauracion de BD) para reducir la perdida.
                                6. Verificar portal, login y monitoreo; reactivar el cron de respaldos.
                                7. Resolver el incidente y documentar el post-mortem.

                                METAS
                                RTO objetivo del Droplet: 2 horas. La copia externa en PC cubre el peor caso
                                (perdida total del proveedor).""")
                        .autor("Equipo TI Cat Jard").vistas(0).build(),
                ArticuloKB.builder()
                        .codigo("KB-" + year + "-007")
                        .titulo("Runbook: alerta de CPU/memoria/disco alto en el Droplet")
                        .categoria(CategoriaKB.runbook)
                        .resumen("Que hacer cuando el monitoreo genera un evento alto o critico de recursos: diagnostico y mitigacion sin tumbar el servicio.")
                        .categoriaIncidente(CategoriaIncidente.rendimiento)
                        .servicioId(dropletId)
                        .contenido("""
                                ESCENARIO
                                El modulo de eventos creo un incidente automatico por CPU, memoria, load o disco
                                por encima del umbral (alto >= 80-85 %, critico >= 90-95 %).

                                PASOS (en el Droplet, por SSH)
                                1. Ver el estado general: docker stats --no-stream  y  free -h  /  df -h
                                2. Identificar el contenedor que consume de mas (RAM suele ser una JVM).
                                3. Si es un microservicio puntual: docker compose ... restart <servicio>
                                   (los limites mem_limit evitan que uno tumbe a los demas).
                                4. Si es disco: depurar logs e imagenes viejas:
                                   docker system prune -af --volumes=false  y revisar ~/catjard-backups.
                                5. Si el consumo es legitimo y sostenido: evaluar resize del Droplet desde el
                                   panel de DigitalOcean (sin perder datos).
                                6. Confirmar en el panel de monitoreo que la metrica volvio a verde (evento de
                                   recuperacion) y resolver el incidente.

                                NO HACER
                                No reiniciar el contenedor de Postgres como primera medida: revisar primero que
                                servicio genera la carga.""")
                        .autor("Equipo TI Cat Jard").vistas(0).build(),
                ArticuloKB.builder()
                        .codigo("KB-" + year + "-008")
                        .titulo("Runbook: error 500 \"viola la restriccion check ..._check\" al guardar")
                        .categoria(CategoriaKB.runbook)
                        .resumen("Caso real (2026-07-10): inserts que fallan por CHECK de enum desactualizados de Hibernate. Causa raiz y solucion definitiva.")
                        .categoriaIncidente(CategoriaIncidente.aplicaciones)
                        .contenido("""
                                ESCENARIO (CASO REAL, 2026-07-10)
                                Al sincronizar respaldos con DigitalOcean, el backend devolvia 500:
                                "new row for relation respaldos violates check constraint respaldos_origen_check".

                                CAUSA RAIZ
                                Flyway no corre en este proyecto (Spring Boot 4 lo requiere como modulo aparte),
                                asi que el esquema lo maneja Hibernate (ddl-auto=update). Hibernate crea CHECK de
                                enums al crear cada tabla y NO los actualiza cuando el enum gana valores nuevos:
                                el CHECK viejo rechaza el valor nuevo (aqui: origen 'digitalocean').

                                SOLUCION
                                1. Ejecutar el script idempotente que elimina los CHECK de enum de Hibernate:
                                   Local:   psql -U postgres -d catjard_solicitudes -f scripts/post-deploy-drop-enum-checks.sql
                                   Droplet: docker exec -i catjard-postgres-1 psql -U postgres -d catjard_solicitudes
                                            < scripts/post-deploy-drop-enum-checks.sql
                                2. Reintentar la operacion (no hace falta reiniciar: el arreglo es en la BD).

                                PREVENCION
                                Correr ese script como paso 0 de todo despliegue. La validacion de enums la hace
                                la aplicacion al parsear; los CHECK de Hibernate no aportan y se desactualizan.""")
                        .autor("Equipo TI Cat Jard").vistas(0).build(),
                ArticuloKB.builder()
                        .codigo("KB-" + year + "-009")
                        .titulo("Politica de umbrales y clasificacion de eventos")
                        .categoria(CategoriaKB.monitoreo_eventos)
                        .resumen("Como el monitoreo clasifica en Informacion/Advertencia/Alto/Critico y cuando un evento escala a incidente y a Jira.")
                        .contenido("""
                                UMBRALES POR METRICA (advertencia / alto / critico)
                                - CPU: 60 / 80 / 90 %
                                - Memoria RAM: 70 / 85 / 95 %
                                - Disco: 80 / 90 / 95 %
                                - Load (5 min): 5 / 10 / 15
                                - Red (entrada/salida): 30 / 50 / 100 Mbps

                                CLASIFICACION Y ESCALAMIENTO
                                - Informacion: operacion normal o recuperacion tras una alerta. Solo historial.
                                - Advertencia: en observacion (proactivo). NO genera incidente.
                                - Alto / Critico: degradacion o riesgo de indisponibilidad. Generan incidente
                                  automatico (origen monitoreo) con el contador RTO del servicio de infraestructura.
                                - A Jira (GDICJ) se escala con el boton del panel — pensado para los criticos —
                                  y el ticket incluye la estrategia documentada de esta Base de Conocimiento.

                                ANTI-RUIDO
                                Si la condicion sigue igual dentro del cooldown (15 min) no se duplica el evento;
                                una lectura normal solo se registra como "recuperacion" despues de una alerta.""")
                        .autor("Equipo TI Cat Jard").vistas(0).build(),
                ArticuloKB.builder()
                        .codigo("KB-" + year + "-010")
                        .titulo("Politica de gestion de incidencias (ciclo de vida y prioridades)")
                        .categoria(CategoriaKB.gestion_incidencias)
                        .resumen("Flujo registrado -> diagnostico -> resolucion -> cierre, matriz de priorizacion ITIL y sincronizacion con Jira.")
                        .contenido("""
                                CICLO DE VIDA
                                registrado -> en diagnostico -> en resolucion -> resuelto -> cerrado
                                (con reabierto y cancelado como estados de excepcion).

                                PRIORIZACION (matriz ITIL Impacto x Urgencia)
                                alto x alta = critica; alto x media o medio x alta = alta;
                                combinaciones medias = media; el resto = baja.

                                REGLAS
                                - Todo evento alto/critico del monitoreo se registra como incidente automatico.
                                - Si el incidente esta asociado a un servicio del catalogo, corre su contador RTO
                                  y al resolver queda medido el cumplimiento.
                                - Jira (tablero GDICJ) es la fuente de verdad del estado: el panel sincroniza cada 30 s.
                                - Al resolver: registrar diagnostico (causa raiz), solucion y evidencia. Si el caso
                                  es repetible, documentarlo como runbook en esta Base de Conocimiento.""")
                        .autor("Equipo TI Cat Jard").vistas(0).build(),
                ArticuloKB.builder()
                        .codigo("KB-" + year + "-011")
                        .titulo("Runbook: restaurar la base de datos desde un respaldo (consola)")
                        .categoria(CategoriaKB.runbook)
                        .resumen("Recuperar una BD (o las 7) desde un dump pg_dump por consola: en local, en el Droplet (docker exec) y desde la copia local descargada.")
                        .categoriaIncidente(CategoriaIncidente.aplicaciones)
                        .servicioId(bdId)
                        .contenido("""
                                ESCENARIO
                                Se perdieron o corrompieron datos (borrado accidental, fallo de disco, migracion).
                                Hay que restaurar desde un respaldo pg_dump (.dump, formato custom).

                                DE DONDE SALE EL RESPALDO
                                - Dump diario automatico: C:\\catjard-backups\\dumps\\ (o el cron del Droplet).
                                - Copia local (boton "Descargar copia local"): un ZIP con un .dump por cada una de
                                  las 7 BDs (catjard_identity, _catalog, _crm, _sales, _inventory, _operations,
                                  _solicitudes). Descomprimir el ZIP para obtener los .dump.

                                RESTAURAR UNA BD EN LOCAL (Windows, consola)
                                Se restaura primero a una BD NUEVA para no pisar la productiva:
                                1. Crear la BD destino:
                                   psql -U postgres -c "CREATE DATABASE catjard_sales_restore;"
                                2. Restaurar (formato custom -> pg_restore):
                                   pg_restore -U postgres -d catjard_sales_restore --no-owner --no-acl catjard_sales.dump
                                3. Verificar:
                                   psql -U postgres -d catjard_sales_restore -c "SELECT count(*) FROM pedidos;"
                                4. Swap consciente (apagar antes el microservicio que usa esa BD):
                                   ALTER DATABASE catjard_sales RENAME TO catjard_sales_old;
                                   ALTER DATABASE catjard_sales_restore RENAME TO catjard_sales;
                                   Levantar el servicio y, ya confirmado, DROP DATABASE catjard_sales_old.

                                RESTAURAR EN LA NUBE (Droplet, por SSH — Postgres corre en un contenedor)
                                El cliente pg_restore vive DENTRO del contenedor de la BD; se le pasa el .dump por STDIN:
                                1. Subir el .dump al Droplet (scp) o descomprimir alli el ZIP de la copia local.
                                2. Crear la BD destino:
                                   docker exec -i catjard-postgres-1 psql -U postgres -c "CREATE DATABASE catjard_sales_restore;"
                                3. Restaurar pasando el archivo por STDIN:
                                   docker exec -i catjard-postgres-1 pg_restore -U postgres -d catjard_sales_restore --no-owner --no-acl < catjard_sales.dump
                                4. Verificar y hacer el swap igual que en local (apagar antes con: docker compose ... stop <servicio>).

                                RESTAURAR LAS 7 BDs (desastre mayor)
                                Repetir el procedimiento con cada .dump del ZIP. Son independientes; conviene empezar
                                por catjard_identity para recuperar primero el login.

                                PROBAR EL PLAN (importante)
                                Un backup que nunca se restauro no es un backup. Al menos una vez al mes: restaurar el
                                ultimo dump de identity a una BD throwaway, contar filas y dropearla.

                                NOTA
                                Los .dump son formato custom (pg_dump -F c): se restauran con pg_restore, permiten
                                restore selectivo (--table=<tabla>) y no pisan owners/permisos (--no-owner --no-acl).""")
                        .autor("Equipo TI Cat Jard").vistas(0).build()
        ));

        log.info("DataSeeder: Base de Conocimiento sembrada (10 articulos).");
    }

    private Long servicioPorCodigo(String codigo) {
        return servicioRepo.findByCodigoStartingWithOrderByCodigoDesc(codigo).stream()
                .findFirst().map(ServicioCritico::getId).orElse(null);
    }
}
