package com.catjard.solicitudes.service;

import com.catjard.solicitudes.dto.RegistrarRespaldoDTO;
import com.catjard.solicitudes.model.OrigenRespaldo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Exportacion de un respaldo completo descargable al equipo (la "1" de la regla 3-2-1:
 * una copia FUERA del servidor). Lanza pg_dump sobre las 7 BDs del cluster local, arma
 * un ZIP con un .dump por base (formato custom, restaurable con pg_restore) y registra
 * el respaldo para trazabilidad del plan de continuidad.
 *
 * Seguridad: los nombres de BD son constantes (no vienen del request), pg_dump se invoca
 * con lista de argumentos (sin shell -> sin inyeccion) y la password viaja por la variable
 * de entorno PGPASSWORD, no en la linea de comando.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BackupExportService {

    // Las 7 BDs del cluster (init-databases.sql). Los scripts .ps1 olvidan solicitudes;
    // aca se incluye para que la copia externa cubra Continuidad/Incidentes/Eventos.
    private static final List<String> BASES = List.of(
            "catjard_identity",
            "catjard_catalog",
            "catjard_crm",
            "catjard_sales",
            "catjard_inventory",
            "catjard_operations",
            "catjard_solicitudes");

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final int TIMEOUT_SEG_POR_BD = 120;

    private final ContinuidadService continuidad;

    @Value("${backup.pg-dump-path:C:\\Program Files\\PostgreSQL\\18\\bin\\pg_dump.exe}")
    private String pgDumpPath;
    @Value("${backup.db-host:localhost}")
    private String dbHost;
    @Value("${backup.db-port:5432}")
    private String dbPort;
    @Value("${spring.datasource.username:postgres}")
    private String dbUser;
    @Value("${spring.datasource.password:postgres}")
    private String dbPassword;

    public record Export(byte[] zip, String filename, double tamanoMb, int duracionSeg) {}

    // Genera el ZIP con los 7 dumps y registra el respaldo (copia externa). No es
    // transaccional: pg_dump puede tardar y no queremos una transaccion abierta
    // mientras corre; el registro se hace al final en su propia transaccion corta.
    public Export exportarTodas() {
        validarPgDump();
        long inicio = System.currentTimeMillis();

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(buffer)) {
            for (String db : BASES) {
                byte[] dump = ejecutarPgDump(db);
                zip.putNextEntry(new ZipEntry(db + ".dump"));
                zip.write(dump);
                zip.closeEntry();
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo armar el ZIP del respaldo: " + e.getMessage(), e);
        }

        byte[] zipBytes = buffer.toByteArray();
        int duracionSeg = (int) ((System.currentTimeMillis() - inicio) / 1000);
        double tamanoMb = Math.round(zipBytes.length / 1048576.0 * 100) / 100.0;
        String filename = "catjard-backup-" + LocalDateTime.now().format(TS) + ".zip";

        registrarRespaldo(tamanoMb, duracionSeg);
        log.info("Respaldo local exportado: {} ({} MB, {} s, {} BDs)",
                filename, tamanoMb, duracionSeg, BASES.size());
        return new Export(zipBytes, filename, tamanoMb, duracionSeg);
    }

    // Falla temprano y claro si la ruta absoluta configurada no apunta a un ejecutable.
    // Si es un nombre simple (sin separador) se confia en el PATH y se deja fallar al start().
    private void validarPgDump() {
        boolean esRuta = pgDumpPath.contains("\\") || pgDumpPath.contains("/");
        if (esRuta && !new File(pgDumpPath).isFile()) {
            throw new IllegalStateException(
                    "No se encontro pg_dump en '" + pgDumpPath + "'. Ajusta la propiedad "
                    + "backup.pg-dump-path (o la variable de entorno BACKUP_PG_DUMP_PATH) "
                    + "a la ruta real de pg_dump.exe.");
        }
    }

    // pg_dump -F c de UNA base, capturando el dump por stdout. stderr se drena en un
    // hilo aparte para no bloquear el pipe (y para no mezclar texto con el dump binario).
    // Package-private para poder sobreescribirlo en tests sin lanzar un proceso real.
    byte[] ejecutarPgDump(String db) {
        ProcessBuilder pb = new ProcessBuilder(
                pgDumpPath,
                "--host=" + dbHost,
                "--port=" + dbPort,
                "--username=" + dbUser,
                "--format=custom",
                "--compress=6",
                "--no-owner",
                "--no-acl",
                db);
        pb.environment().put("PGPASSWORD", dbPassword);

        Process p;
        try {
            p = pb.start();
        } catch (IOException e) {
            throw new IllegalStateException(
                    "No se pudo ejecutar pg_dump ('" + pgDumpPath + "'): " + e.getMessage()
                    + ". Verifica que PostgreSQL este instalado y la propiedad backup.pg-dump-path.", e);
        }

        ByteArrayOutputStream err = new ByteArrayOutputStream();
        Thread drenaErr = new Thread(() -> {
            try { p.getErrorStream().transferTo(err); } catch (IOException ignored) { /* proceso terminando */ }
        });
        drenaErr.setDaemon(true);
        drenaErr.start();

        byte[] dump;
        boolean termino;
        try {
            dump = p.getInputStream().readAllBytes();
            termino = p.waitFor(TIMEOUT_SEG_POR_BD, TimeUnit.SECONDS);
            drenaErr.join(2000);
        } catch (IOException | InterruptedException e) {
            p.destroyForcibly();
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            throw new IllegalStateException("Fallo la lectura de pg_dump para " + db + ": " + e.getMessage(), e);
        }

        if (!termino) {
            p.destroyForcibly();
            throw new IllegalStateException("pg_dump excedio el tiempo limite (" + TIMEOUT_SEG_POR_BD
                    + " s) respaldando " + db + ".");
        }
        if (p.exitValue() != 0) {
            String detalle = err.toString(StandardCharsets.UTF_8).trim();
            throw new IllegalStateException("pg_dump fallo respaldando " + db
                    + (detalle.isEmpty() ? "." : ": " + detalle));
        }
        return dump;
    }

    private void registrarRespaldo(double tamanoMb, int duracionSeg) {
        continuidad.registrarRespaldo(new RegistrarRespaldoDTO(
                null,                      // fechaHora = ahora
                null,                      // sin servicio puntual: cubre todo el cluster
                BASES.size() + " BDs PostgreSQL (pg_dump -F c, ZIP)",
                "completo",
                "copia_externa",
                "exitoso",
                tamanoMb,
                duracionSeg,
                "Descarga local desde el panel de gerencia (regla 3-2-1)"),
                OrigenRespaldo.manual);
    }
}
