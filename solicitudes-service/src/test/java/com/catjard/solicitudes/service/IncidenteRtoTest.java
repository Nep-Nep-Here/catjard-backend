package com.catjard.solicitudes.service;

import com.catjard.solicitudes.dto.ActualizarIncidenteDTO;
import com.catjard.solicitudes.jira.JiraService;
import com.catjard.solicitudes.model.EstadoIncidente;
import com.catjard.solicitudes.model.Incidente;
import com.catjard.solicitudes.repository.IncidenteRepository;
import com.catjard.solicitudes.repository.ServicioCriticoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests del contador RTO del incidente (Gestion de Continuidad).
 *
 * Escenario de negocio: un servicio critico con RTO de 2 h. El contador arranca al
 * asociar el incidente al servicio y debe DETENERSE cuando el incidente termina,
 * ya sea porque se resolvio aca o porque lo completaron en el tablero GDICJ de Jira.
 */
@ExtendWith(MockitoExtension.class)
class IncidenteRtoTest {

    @Mock IncidenteRepository repo;
    @Mock ServicioCriticoRepository servicioRepo;
    @Mock BaseConocimientoService baseConocimiento;
    @Mock JiraService jira;
    @InjectMocks IncidenteService service;

    private static final ActualizarIncidenteDTO NADA =
            new ActualizarIncidenteDTO(null, null, null, null, null, null, null, null, null, null);

    private ActualizarIncidenteDTO cambiarEstadoA(String estado) {
        return new ActualizarIncidenteDTO(estado, null, null, null, null, null, null, null, null, null);
    }

    // Incidente con contador RTO de 2 h corriendo, abierto desde hace `abiertoHaceMin`.
    private Incidente incidenteConRto(int abiertoHaceMin, EstadoIncidente estado) {
        LocalDateTime inicio = LocalDateTime.now().minusMinutes(abiertoHaceMin);
        Incidente i = Incidente.builder()
                .id(1L).codigo("INC-2026-001").estado(estado)
                .servicioId(9L).servicioNombre("Droplet DigitalOcean")
                .rtoMinutos(120)
                .rtoDeadline(inicio.plusMinutes(120))
                .build();
        i.setFechaCreacion(inicio);
        return i;
    }

    @Test
    @DisplayName("resolver dentro de las 2 h: cumplio el RTO (check verde)")
    void resolverDentroDelPlazoCumple() {
        Incidente i = incidenteConRto(30, EstadoIncidente.en_resolucion);
        when(repo.findById(1L)).thenReturn(Optional.of(i));

        service.actualizar(1L, cambiarEstadoA("resuelto"), "soporte");

        assertThat(i.getCumplioRto()).isTrue();
        assertThat(i.getFechaResolucion()).isNotNull();
    }

    @Test
    @DisplayName("resolver pasadas las 2 h: RTO incumplido (queda registrado)")
    void resolverFueraDelPlazoIncumple() {
        Incidente i = incidenteConRto(180, EstadoIncidente.en_resolucion);
        when(repo.findById(1L)).thenReturn(Optional.of(i));

        service.actualizar(1L, cambiarEstadoA("resuelto"), "soporte");

        assertThat(i.getCumplioRto()).isFalse();
    }

    @Test
    @DisplayName("cancelar un falso positivo apaga el contador y no lo mide")
    void cancelarApagaElContador() {
        Incidente i = incidenteConRto(300, EstadoIncidente.registrado);  // ya vencido
        when(repo.findById(1L)).thenReturn(Optional.of(i));

        service.actualizar(1L, cambiarEstadoA("cancelado"), "soporte");

        assertThat(i.getEstado()).isEqualTo(EstadoIncidente.cancelado);
        assertThat(i.getCumplioRto()).isNull();       // ni cumplido ni incumplido: N/A
        assertThat(i.getFechaCierre()).isNotNull();   // terminal: el contador se detiene
    }

    @Test
    @DisplayName("cancelar un incidente ya resuelto lo saca de las metricas de continuidad")
    void cancelarIncidenteYaResueltoLoSacaDeMetricas() {
        Incidente i = incidenteConRto(180, EstadoIncidente.resuelto);
        i.setFechaResolucion(LocalDateTime.now());
        i.setCumplioRto(false);  // ya media incumplido
        when(repo.findById(1L)).thenReturn(Optional.of(i));

        service.actualizar(1L, cambiarEstadoA("cancelado"), "soporte");

        assertThat(i.getCumplioRto()).isNull();  // no revive el incumplimiento
    }

    @Test
    @DisplayName("Jira: columna con nombre propio pero categoria 'done' cierra el incidente y congela el RTO")
    void jiraCompletadoCierraYCongelaRto() {
        Incidente i = incidenteConRto(30, EstadoIncidente.en_resolucion);
        i.setJiraIssueKey("GDICJ-15");
        when(jira.isEnabled()).thenReturn(true);
        when(repo.findAll()).thenReturn(List.of(i));
        // "Completado" no esta en el mapa de nombres: antes se ignoraba y el contador seguia.
        when(jira.obtenerEstado("GDICJ-15")).thenReturn(new JiraService.JiraEstado("Completado", "done"));

        int actualizadas = service.sincronizarConJira();

        assertThat(actualizadas).isEqualTo(1);
        assertThat(i.getEstado()).isEqualTo(EstadoIncidente.cerrado);
        assertThat(i.getCumplioRto()).isTrue();   // se completo dentro de las 2 h
        assertThat(i.getFechaCierre()).isNotNull();
    }

    @Test
    @DisplayName("Jira: columna 'Listo' sigue cerrando por nombre (no rompimos el mapeo previo)")
    void jiraListoCierraPorNombre() {
        Incidente i = incidenteConRto(30, EstadoIncidente.en_resolucion);
        i.setJiraIssueKey("GDICJ-16");
        when(jira.isEnabled()).thenReturn(true);
        when(repo.findAll()).thenReturn(List.of(i));
        when(jira.obtenerEstado("GDICJ-16")).thenReturn(new JiraService.JiraEstado("Listo", "done"));

        service.sincronizarConJira();

        assertThat(i.getEstado()).isEqualTo(EstadoIncidente.cerrado);
    }

    @Test
    @DisplayName("Jira: columna desconocida en curso ('indeterminate') no toca el incidente")
    void jiraColumnaEnCursoDesconocidaNoTocaElIncidente() {
        Incidente i = incidenteConRto(30, EstadoIncidente.registrado);
        i.setJiraIssueKey("GDICJ-17");
        when(jira.isEnabled()).thenReturn(true);
        when(repo.findAll()).thenReturn(List.of(i));
        when(jira.obtenerEstado("GDICJ-17")).thenReturn(new JiraService.JiraEstado("Esperando proveedor", "indeterminate"));

        int actualizadas = service.sincronizarConJira();

        assertThat(actualizadas).isZero();
        assertThat(i.getEstado()).isEqualTo(EstadoIncidente.registrado);  // conservador
    }

    @Test
    @DisplayName("reabrir un incidente cerrado reactiva el contador")
    void reabrirReactivaElContador() {
        Incidente i = incidenteConRto(30, EstadoIncidente.cerrado);
        i.setFechaResolucion(LocalDateTime.now());
        i.setFechaCierre(LocalDateTime.now());
        i.setCumplioRto(true);
        when(repo.findById(1L)).thenReturn(Optional.of(i));

        service.actualizar(1L, cambiarEstadoA("reabierto"), "soporte");

        assertThat(i.getCumplioRto()).isNull();
        assertThat(i.getFechaResolucion()).isNull();
    }
}
