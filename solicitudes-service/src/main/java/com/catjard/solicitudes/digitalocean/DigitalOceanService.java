package com.catjard.solicitudes.digitalocean;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Cliente de la API de DigitalOcean (Monitoring): lee las metricas reales del Droplet
// y las politicas de alerta configuradas en el panel de DO.
// Igual que JiraService, todo es BEST-EFFORT: si DO falla, el caller captura la excepcion.
@Slf4j
@Service
public class DigitalOceanService {

    private static final String BASE_URL = "https://api.digitalocean.com";

    private final DigitalOceanProperties props;
    private final RestClient http;

    public DigitalOceanService(DigitalOceanProperties props) {
        this.props = props;
        var builder = RestClient.builder().baseUrl(BASE_URL);
        if (props.apiToken() != null && !props.apiToken().isBlank()) {
            builder.defaultHeader("Authorization", "Bearer " + props.apiToken());
        }
        this.http = builder.build();
    }

    // Habilitado solo si hay flag + token: sin token nunca se llama a DO.
    public boolean isEnabled() {
        return props.enabled() && props.apiToken() != null && !props.apiToken().isBlank();
    }

    public record Droplet(String id, String nombre) {}

    // Serie de una metrica estilo Prometheus: labels + puntos [timestamp, valor].
    public record Serie(Map<String, String> labels, List<double[]> valores) {}

    // Droplet a monitorear: el configurado en 'monitoreo.droplet-id' o el primero de la cuenta.
    @SuppressWarnings("unchecked")
    public Droplet obtenerDroplet() {
        Map<String, Object> resp = http.get()
                .uri("/v2/droplets?per_page=50")
                .retrieve()
                .body(Map.class);
        if (resp == null || !(resp.get("droplets") instanceof List<?> droplets)) return null;
        for (Object o : droplets) {
            if (!(o instanceof Map<?, ?> d)) continue;
            String id = String.valueOf(d.get("id"));
            String nombre = String.valueOf(d.get("name"));
            if (props.dropletId() == null || props.dropletId().isBlank() || props.dropletId().equals(id)) {
                return new Droplet(id, nombre);
            }
        }
        return null;
    }

    // ------------------- Metricas (ultimo valor calculado) -------------------

    // % de CPU en uso. DO entrega contadores acumulados de segundos por modo (idle, user,
    // sys...), asi que se toma una ventana de 10 min y se calcula: 1 - (delta idle / delta total).
    public Double cpuPorcentaje(String hostId) {
        List<Serie> series = consultar("/v2/monitoring/metrics/droplet/cpu", hostId, Map.of());
        if (series.isEmpty()) return null;

        double totalDelta = 0, idleDelta = 0;
        for (Serie s : series) {
            List<double[]> v = s.valores();
            if (v.size() < 2) continue;
            double delta = v.get(v.size() - 1)[1] - v.get(0)[1];
            totalDelta += delta;
            if ("idle".equalsIgnoreCase(s.labels().getOrDefault("mode", ""))) idleDelta += delta;
        }
        if (totalDelta <= 0) return null;
        return redondear((1.0 - idleDelta / totalDelta) * 100.0);
    }

    // % de RAM en uso = (total - disponible) / total.
    public Double memoriaPorcentaje(String hostId) {
        Double total = ultimoValor("/v2/monitoring/metrics/droplet/memory_total", hostId, Map.of(), null);
        Double libre = ultimoValor("/v2/monitoring/metrics/droplet/memory_available", hostId, Map.of(), null);
        if (libre == null) libre = ultimoValor("/v2/monitoring/metrics/droplet/memory_free", hostId, Map.of(), null);
        if (total == null || libre == null || total <= 0) return null;
        return redondear((1.0 - libre / total) * 100.0);
    }

    // % del filesystem raiz en uso = (size - free) / size.
    public Double discoPorcentaje(String hostId) {
        Double size = ultimoValor("/v2/monitoring/metrics/droplet/filesystem_size", hostId, Map.of(), "/");
        Double free = ultimoValor("/v2/monitoring/metrics/droplet/filesystem_free", hostId, Map.of(), "/");
        if (size == null || free == null || size <= 0) return null;
        return redondear((1.0 - free / size) * 100.0);
    }

    // Load average de 5 minutos (comparable con la alerta "5 Minute Load Average" de DO).
    public Double load5(String hostId) {
        Double v = ultimoValor("/v2/monitoring/metrics/droplet/load_5", hostId, Map.of(), null);
        return v != null ? redondear(v) : null;
    }

    // Ancho de banda publico en Mbps; direction = "inbound" | "outbound".
    public Double anchoBandaMbps(String hostId, String direction) {
        Double v = ultimoValor("/v2/monitoring/metrics/droplet/bandwidth", hostId,
                Map.of("interface", "public", "direction", direction), null);
        return v != null ? redondear(v) : null;
    }

    // Politicas de alerta configuradas en el panel de DO (para mostrarlas en el front).
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> politicasAlerta() {
        Map<String, Object> resp = http.get()
                .uri("/v2/monitoring/alerts")
                .retrieve()
                .body(Map.class);
        if (resp == null || !(resp.get("policies") instanceof List<?> policies)) return List.of();
        List<Map<String, Object>> out = new ArrayList<>();
        for (Object o : policies) {
            if (o instanceof Map<?, ?> p) out.add((Map<String, Object>) p);
        }
        return out;
    }

    // ------------------- helpers -------------------

    // Ultimo punto de la metrica; si 'mountpoint' viene, filtra esa serie (filesystem).
    private Double ultimoValor(String path, String hostId, Map<String, String> extra, String mountpoint) {
        List<Serie> series = consultar(path, hostId, extra);
        Serie elegida = null;
        for (Serie s : series) {
            if (mountpoint == null || mountpoint.equals(s.labels().get("mountpoint"))) {
                elegida = s;
                break;
            }
        }
        if (elegida == null && !series.isEmpty()) elegida = series.get(0);
        if (elegida == null || elegida.valores().isEmpty()) return null;
        return elegida.valores().get(elegida.valores().size() - 1)[1];
    }

    // GET al endpoint de metricas con ventana de 10 minutos; parsea el formato
    // {"data":{"result":[{"metric":{...},"values":[[ts,"v"],...]},...]}}.
    @SuppressWarnings("unchecked")
    private List<Serie> consultar(String path, String hostId, Map<String, String> extra) {
        long end = Instant.now().getEpochSecond();
        long start = end - 600;

        StringBuilder uri = new StringBuilder(path)
                .append("?host_id=").append(hostId)
                .append("&start=").append(start)
                .append("&end=").append(end);
        extra.forEach((k, v) -> uri.append("&").append(k).append("=").append(v));

        Map<String, Object> resp = http.get().uri(uri.toString()).retrieve().body(Map.class);
        if (resp == null || !(resp.get("data") instanceof Map<?, ?> data)) return List.of();
        if (!(data.get("result") instanceof List<?> result)) return List.of();

        List<Serie> series = new ArrayList<>();
        for (Object o : result) {
            if (!(o instanceof Map<?, ?> r)) continue;
            Map<String, String> labels = new java.util.HashMap<>();
            if (r.get("metric") instanceof Map<?, ?> m) {
                m.forEach((k, v) -> labels.put(String.valueOf(k), String.valueOf(v)));
            }
            List<double[]> valores = new ArrayList<>();
            if (r.get("values") instanceof List<?> vals) {
                for (Object p : vals) {
                    if (p instanceof List<?> par && par.size() >= 2) {
                        try {
                            double ts = Double.parseDouble(String.valueOf(par.get(0)));
                            double val = Double.parseDouble(String.valueOf(par.get(1)));
                            valores.add(new double[]{ts, val});
                        } catch (NumberFormatException ignored) { }
                    }
                }
            }
            series.add(new Serie(labels, valores));
        }
        return series;
    }

    private static double redondear(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
