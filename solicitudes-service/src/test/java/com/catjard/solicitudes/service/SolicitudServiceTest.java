package com.catjard.solicitudes.service;

import com.catjard.solicitudes.dto.ActualizarSolicitudDTO;
import com.catjard.solicitudes.dto.CrearSolicitudDTO;
import com.catjard.solicitudes.dto.SolicitudDTO;
import com.catjard.solicitudes.jira.JiraService;
import com.catjard.solicitudes.model.EstadoSolicitud;
import com.catjard.solicitudes.model.Prioridad;
import com.catjard.solicitudes.model.Solicitud;
import com.catjard.solicitudes.model.TipoSolicitud;
import com.catjard.solicitudes.repository.SolicitudRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios para SolicitudService (mesa de ayuda / tickets).
 * Cubre el alta de solicitudes (codigo autogenerado, estado inicial, prioridad por defecto),
 * la integracion best-effort con Jira (incluyendo su fallo), la gestion por el admin
 * y los casos de no-encontrado.
 */
@ExtendWith(MockitoExtension.class)
class SolicitudServiceTest {

    @Mock SolicitudRepository repo;
    @Mock JiraService jira;

    @InjectMocks SolicitudService service;

    @Test
    @DisplayName("crear OK: genera codigo correlativo, estado 'nueva' y guarda el key de Jira")
    void crearOk() {
        var dto = new CrearSolicitudDTO("servicio", "No carga el catalogo", "Da error 500 al entrar", "alta", null, null);
        when(repo.findByCodigoStartingWithOrderByCodigoDesc(anyString())).thenReturn(List.of());
        when(repo.save(any(Solicitud.class))).thenAnswer(inv -> {
            Solicitud s = inv.getArgument(0);
            s.setId(10L);
            return s;
        });
        when(jira.crearIssue(any(Solicitud.class)))
                .thenReturn(new JiraService.JiraIssue("SUP-1", "https://catjard.atlassian.net/browse/SUP-1"));

        SolicitudDTO out = service.crear(dto, "cliente@demo.com", "Cliente Demo");

        assertThat(out.tipo()).isEqualTo("servicio");
        assertThat(out.estado()).isEqualTo("por_hacer");
        assertThat(out.prioridad()).isEqualTo("alta");
        assertThat(out.codigo()).matches("SOL-\\d{4}-0001");
        assertThat(out.solicitanteEmail()).isEqualTo("cliente@demo.com");
        assertThat(out.jiraIssueKey()).isEqualTo("SUP-1");
        verify(repo).save(any(Solicitud.class));
        verify(jira).crearIssue(any(Solicitud.class));
    }

    @Test
    @DisplayName("crear: prioridad por defecto 'media' cuando no se envia, y sin Jira (deshabilitado)")
    void crearPrioridadPorDefectoSinJira() {
        var dto = new CrearSolicitudDTO("informacion", "Consulta de precios", "Quiero cotizar polos", null, null, null);
        when(repo.findByCodigoStartingWithOrderByCodigoDesc(anyString())).thenReturn(List.of());
        when(repo.save(any(Solicitud.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jira.crearIssue(any(Solicitud.class))).thenReturn(null); // integracion apagada

        SolicitudDTO out = service.crear(dto, "vendedor@demo.com", "Vendedor Demo");

        assertThat(out.prioridad()).isEqualTo("media");
        assertThat(out.tipo()).isEqualTo("informacion");
        assertThat(out.jiraIssueKey()).isNull();
    }

    @Test
    @DisplayName("crear degradado: si Jira falla, la solicitud se crea igual (best-effort) sin key")
    void crearJiraFallaBestEffort() {
        var dto = new CrearSolicitudDTO("acceso", "No puedo entrar", "Olvide mi contrasena", "alta", null, null);
        when(repo.findByCodigoStartingWithOrderByCodigoDesc(anyString())).thenReturn(List.of());
        when(repo.save(any(Solicitud.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jira.crearIssue(any(Solicitud.class))).thenThrow(new RuntimeException("Jira caido"));

        SolicitudDTO out = service.crear(dto, "cliente@demo.com", "Cliente Demo");

        assertThat(out.tipo()).isEqualTo("acceso");
        assertThat(out.estado()).isEqualTo("por_hacer");
        assertThat(out.jiraIssueKey()).isNull();
        verify(repo).save(any(Solicitud.class));
    }

    @Test
    @DisplayName("crear KO: tipo invalido lanza IllegalArgumentException y no persiste")
    void crearTipoInvalido() {
        var dto = new CrearSolicitudDTO("otro", "X", "Y", "media", null, null);

        assertThatThrownBy(() -> service.crear(dto, "u@demo.com", "U"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tipo invalido");

        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("actualizar OK: cambia estado y respuesta, fija atendidoPor y comenta en Jira")
    void actualizarOk() {
        Solicitud existente = Solicitud.builder()
                .id(1L).codigo("SOL-2026-0005")
                .tipo(TipoSolicitud.servicio).prioridad(Prioridad.media)
                .estado(EstadoSolicitud.por_hacer)
                .solicitanteEmail("cliente@demo.com")
                .jiraIssueKey("SUP-9")
                .build();
        when(repo.findById(1L)).thenReturn(Optional.of(existente));

        var dto = new ActualizarSolicitudDTO("en_curso", "alta", "Estamos revisando tu caso.");
        SolicitudDTO out = service.actualizar(1L, dto, "gerente@demo.com");

        assertThat(out.estado()).isEqualTo("en_curso");
        assertThat(out.prioridad()).isEqualTo("alta");
        assertThat(out.respuesta()).isEqualTo("Estamos revisando tu caso.");
        assertThat(out.atendidoPor()).isEqualTo("gerente@demo.com");
        verify(jira).agregarComentario(eq("SUP-9"), anyString());
    }

    @Test
    @DisplayName("obtener KO: IllegalArgumentException cuando la solicitud no existe")
    void obtenerNoEncontrado() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtener(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Solicitud no encontrada");
    }

    @Test
    @DisplayName("eliminar KO: IllegalArgumentException cuando la solicitud no existe")
    void eliminarNoEncontrado() {
        when(repo.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.eliminar(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Solicitud no encontrada");

        verify(repo, never()).deleteById(any());
    }
}
