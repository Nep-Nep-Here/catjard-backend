package com.catjard.solicitudes.jira;

import com.catjard.solicitudes.model.Cambio;
import com.catjard.solicitudes.model.Incidente;
import com.catjard.solicitudes.model.Solicitud;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

// Integracion con Jira Cloud (REST API v3).
// - crearIssue(): se llama al crear una solicitud; crea un issue y devuelve su key + url.
// - agregarComentario(): refleja en Jira los cambios de estado / respuestas del admin.
// Todo es BEST-EFFORT: si Jira falla, la solicitud se gestiona igual (el caller captura la excepcion).
@Slf4j
@Service
public class JiraService {

    private final JiraProperties props;
    private final RestClient http;

    public JiraService(JiraProperties props) {
        this.props = props;
        String base = props.baseUrl() != null ? props.baseUrl().replaceAll("/+$", "") : "";
        var builder = RestClient.builder().baseUrl(base);
        if (props.email() != null && props.apiToken() != null
                && !props.email().isBlank() && !props.apiToken().isBlank()) {
            String basic = Base64.getEncoder().encodeToString(
                    (props.email() + ":" + props.apiToken()).getBytes(StandardCharsets.UTF_8));
            builder.defaultHeader("Authorization", "Basic " + basic);
        }
        this.http = builder.build();
    }

    public boolean isEnabled() {
        return props.enabled();
    }

    public record JiraIssue(String key, String url) {}

    // Crea un issue en Jira a partir de la solicitud. Devuelve null si la integracion esta apagada.
    public JiraIssue crearIssue(Solicitud s) {
        if (!props.enabled()) {
            log.debug("Jira deshabilitado: no se crea issue para {}", s.getCodigo());
            return null;
        }

        String summary = "[" + s.getTipo().name().toUpperCase() + "] " + s.getAsunto();
        String cuerpo = "Solicitud " + s.getCodigo()
                + "\nTipo: " + s.getTipo().name()
                + "\nPrioridad: " + s.getPrioridad().name()
                + "\nSolicitante: " + s.getSolicitanteEmail()
                + "\n\n" + s.getDescripcion();

        Map<String, Object> fields = Map.of(
                "project", Map.of("key", props.projectKey()),
                "summary", summary,
                "issuetype", Map.of("name", props.issueType()),
                "labels", List.of("catjard", s.getTipo().name()),
                "description", adf(cuerpo)
        );
        Map<String, Object> body = Map.of("fields", fields);

        @SuppressWarnings("unchecked")
        Map<String, Object> resp = http.post()
                .uri("/rest/api/3/issue")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);

        String key = resp != null ? String.valueOf(resp.get("key")) : null;
        String url = key != null ? props.baseUrl().replaceAll("/+$", "") + "/browse/" + key : null;
        log.info("Jira: issue {} creado para solicitud {}", key, s.getCodigo());
        return new JiraIssue(key, url);
    }

    // Crea un issue en Jira a partir de un Cambio (control de cambios). Devuelve null si esta apagada.
    public JiraIssue crearIssue(Cambio c) {
        if (!props.enabled()) {
            log.debug("Jira deshabilitado: no se crea issue para cambio {}", c.getCodigo());
            return null;
        }

        String summary = "[CAMBIO " + c.getTipoCambio().name().toUpperCase() + "] " + c.getTitulo();
        String cuerpo = "Cambio " + c.getCodigo()
                + "\nProyecto: " + (c.getProyecto() != null ? c.getProyecto() : "-")
                + "\nCategoria: " + c.getCategoria().name()
                + "\nTipo: " + c.getTipoCambio().name()
                + "\nPrioridad: " + c.getPrioridad().name()
                + "\nResponsable: " + (c.getResponsable() != null ? c.getResponsable() : "-")
                + "\nSolicitante: " + c.getSolicitanteEmail()
                + "\n\n" + (c.getDescripcion() != null ? c.getDescripcion() : "");

        Map<String, Object> fields = Map.of(
                "project", Map.of("key", cambiosProjectKey()),
                "summary", summary,
                "issuetype", Map.of("name", props.issueType()),
                "labels", List.of("catjard", "control-de-cambios", c.getCategoria().name()),
                "description", adf(cuerpo)
        );

        @SuppressWarnings("unchecked")
        Map<String, Object> resp = http.post()
                .uri("/rest/api/3/issue")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("fields", fields))
                .retrieve()
                .body(Map.class);

        String key = resp != null ? String.valueOf(resp.get("key")) : null;
        String url = key != null ? props.baseUrl().replaceAll("/+$", "") + "/browse/" + key : null;
        log.info("Jira: issue {} creado para cambio {}", key, c.getCodigo());
        return new JiraIssue(key, url);
    }

    // Crea un issue en Jira a partir de un Incidente (gestion de incidentes -> tablero GDICJ).
    // Devuelve null si la integracion esta apagada.
    public JiraIssue crearIssue(Incidente i) {
        return crearIssue(i, null);
    }

    // Variante con la estrategia documentada de la Base de Conocimiento: la
    // referencia (codigo + titulo del articulo aplicable) viaja en el ticket.
    public JiraIssue crearIssue(Incidente i, String estrategiaKB) {
        if (!props.enabled()) {
            log.debug("Jira deshabilitado: no se crea issue para incidente {}", i.getCodigo());
            return null;
        }

        String summary = "[INC " + i.getPrioridad().name().toUpperCase() + "] " + i.getTitulo();
        String cuerpo = "Incidente " + i.getCodigo()
                + "\nServicio afectado: " + (i.getServicioAfectado() != null ? i.getServicioAfectado() : "-")
                + "\nCategoria: " + i.getCategoria().name()
                + "\nOrigen: " + i.getOrigen().name()
                + "\nImpacto: " + i.getImpacto().name()
                + "\nUrgencia: " + i.getUrgencia().name()
                + "\nPrioridad: " + i.getPrioridad().name()
                + "\nResponsable: " + (i.getResponsable() != null ? i.getResponsable() : "-")
                + "\nReportante: " + i.getSolicitanteEmail()
                + (estrategiaKB != null
                        ? "\n\nEstrategia documentada (Base de Conocimiento): " + estrategiaKB
                        : "")
                + "\n\n" + (i.getDescripcion() != null ? i.getDescripcion() : "");

        Map<String, Object> fields = Map.of(
                "project", Map.of("key", incidentesProjectKey()),
                "summary", summary,
                "issuetype", Map.of("name", props.issueType()),
                "labels", List.of("catjard", "incidente", i.getCategoria().name()),
                "description", adf(cuerpo)
        );

        @SuppressWarnings("unchecked")
        Map<String, Object> resp = http.post()
                .uri("/rest/api/3/issue")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("fields", fields))
                .retrieve()
                .body(Map.class);

        String key = resp != null ? String.valueOf(resp.get("key")) : null;
        String url = key != null ? props.baseUrl().replaceAll("/+$", "") + "/browse/" + key : null;
        log.info("Jira: issue {} creado para incidente {}", key, i.getCodigo());
        return new JiraIssue(key, url);
    }

    // Proyecto Jira para incidentes (tablero GDICJ); cae al de cambios o solicitudes si falta.
    private String incidentesProjectKey() {
        if (props.incidentesProjectKey() != null && !props.incidentesProjectKey().isBlank())
            return props.incidentesProjectKey();
        if (props.cambiosProjectKey() != null && !props.cambiosProjectKey().isBlank())
            return props.cambiosProjectKey();
        return props.projectKey();
    }

    // Agrega un comentario al issue (para reflejar cambios de estado o respuestas).
    public void agregarComentario(String issueKey, String texto) {
        if (!props.enabled() || issueKey == null || issueKey.isBlank()) return;
        http.post()
                .uri("/rest/api/3/issue/{key}/comment", issueKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("body", adf(texto)))
                .retrieve()
                .toBodilessEntity();
        log.info("Jira: comentario agregado al issue {}", issueKey);
    }

    // Proyecto Jira para control de cambios (equipo de sistemas); cae al de solicitudes si falta.
    private String cambiosProjectKey() {
        return (props.cambiosProjectKey() != null && !props.cambiosProjectKey().isBlank())
                ? props.cambiosProjectKey() : props.projectKey();
    }

    public record JiraEstado(String nombre, String categoria) {}

    // Devuelve el estado actual del issue: nombre (ej. "En evaluación") y categoria
    // ("new", "indeterminate", "done"). Null si la integracion esta apagada o fallo la lectura.
    @SuppressWarnings("unchecked")
    public JiraEstado obtenerEstado(String issueKey) {
        if (!props.enabled() || issueKey == null || issueKey.isBlank()) return null;
        Map<String, Object> resp = http.get()
                .uri("/rest/api/3/issue/{k}?fields=status", issueKey)
                .retrieve()
                .body(Map.class);
        if (resp == null) return null;
        Object fields = resp.get("fields");
        if (!(fields instanceof Map<?, ?> f)) return null;
        Object status = f.get("status");
        if (!(status instanceof Map<?, ?> s)) return null;
        String nombre = s.get("name") != null ? s.get("name").toString() : null;
        String categoria = null;
        if (s.get("statusCategory") instanceof Map<?, ?> c && c.get("key") != null) {
            categoria = c.get("key").toString();
        }
        return new JiraEstado(nombre, categoria);
    }

    // Solo la categoria (la usa el sync de solicitudes, que mapea a 3 estados).
    public String obtenerCategoriaEstado(String issueKey) {
        JiraEstado e = obtenerEstado(issueKey);
        return e != null ? e.categoria() : null;
    }

    // Construye el documento ADF (Atlassian Document Format) que exige la API v3.
    private Map<String, Object> adf(String texto) {
        return Map.of(
                "type", "doc",
                "version", 1,
                "content", List.of(Map.of(
                        "type", "paragraph",
                        "content", List.of(Map.of(
                                "type", "text",
                                "text", texto == null ? "" : texto
                        ))
                ))
        );
    }
}
