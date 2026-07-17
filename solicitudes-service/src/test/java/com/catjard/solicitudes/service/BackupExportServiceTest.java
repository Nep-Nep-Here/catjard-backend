package com.catjard.solicitudes.service;

import com.catjard.solicitudes.dto.RegistrarRespaldoDTO;
import com.catjard.solicitudes.model.OrigenRespaldo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

/**
 * Tests del armado del ZIP y el registro del respaldo. Sobreescribe ejecutarPgDump() para
 * no lanzar pg_dump real (no depende de tener PostgreSQL instalado ni del SO): lo que se
 * verifica es la orquestacion (7 entries, registro, validacion de ruta), no pg_dump en si.
 */
class BackupExportServiceTest {

    private ContinuidadService continuidad;

    @BeforeEach
    void setUp() {
        continuidad = mock(ContinuidadService.class);
    }

    // Servicio con un ejecutarPgDump falso que devuelve bytes deterministas por BD.
    private BackupExportService serviceConDumpFake(String pgDumpPath) {
        BackupExportService s = new BackupExportService(continuidad) {
            @Override
            byte[] ejecutarPgDump(String db) {
                return ("FAKEDUMP:" + db).getBytes(StandardCharsets.UTF_8);
            }
        };
        setField(s, "pgDumpPath", pgDumpPath);
        setField(s, "dbHost", "localhost");
        setField(s, "dbPort", "5432");
        setField(s, "dbUser", "postgres");
        setField(s, "dbPassword", "postgres");
        return s;
    }

    @Test
    @DisplayName("arma un ZIP con las 7 BDs y registra el respaldo como copia externa")
    void armaZipYRegistra(@TempDir Path dir) throws IOException {
        // pgDumpPath debe existir para pasar la validacion (no se ejecuta: ejecutarPgDump esta fake).
        Path fake = dir.resolve("pg_dump.exe");
        Files.writeString(fake, "x");
        BackupExportService service = serviceConDumpFake(fake.toString());

        BackupExportService.Export exp = service.exportarTodas();

        List<String> entries = new ArrayList<>();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(exp.zip()))) {
            ZipEntry e;
            while ((e = zis.getNextEntry()) != null) {
                entries.add(e.getName());
                assertThat(new String(zis.readAllBytes(), StandardCharsets.UTF_8))
                        .isEqualTo("FAKEDUMP:" + e.getName().replace(".dump", ""));
            }
        }
        assertThat(entries).hasSize(7)
                .contains("catjard_identity.dump", "catjard_solicitudes.dump");
        assertThat(exp.filename()).startsWith("catjard-backup-").endsWith(".zip");

        ArgumentCaptor<RegistrarRespaldoDTO> cap = ArgumentCaptor.forClass(RegistrarRespaldoDTO.class);
        verify(continuidad, times(1)).registrarRespaldo(cap.capture(), eq(OrigenRespaldo.manual));
        RegistrarRespaldoDTO dto = cap.getValue();
        assertThat(dto.destino()).isEqualTo("copia_externa");
        assertThat(dto.tipo()).isEqualTo("completo");
        assertThat(dto.estado()).isEqualTo("exitoso");
    }

    @Test
    @DisplayName("ruta de pg_dump inexistente: falla claro y NO registra respaldo")
    void rutaInvalidaFalla() {
        BackupExportService service = serviceConDumpFake("C:\\ruta\\que\\no\\existe\\pg_dump.exe");

        assertThatThrownBy(service::exportarTodas)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("backup.pg-dump-path");

        verify(continuidad, times(0)).registrarRespaldo(any(), any());
    }
}
