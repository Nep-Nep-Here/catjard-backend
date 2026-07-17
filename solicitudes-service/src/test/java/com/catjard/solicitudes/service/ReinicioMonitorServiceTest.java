package com.catjard.solicitudes.service;

import com.catjard.solicitudes.model.CategoriaIncidente;
import com.catjard.solicitudes.model.Incidente;
import com.catjard.solicitudes.model.MonitoreoEstado;
import com.catjard.solicitudes.model.Nivel;
import com.catjard.solicitudes.repository.MonitoreoEstadoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Deteccion de reinicios del Droplet. Se prueba la logica pura (procesarBootTime) sin
 * leer /proc/uptime real: un reinicio = el boot time del host cambio respecto al ultimo
 * conocido. Reiniciar solo un contenedor no cambia el boot time del host -> no dispara.
 */
@ExtendWith(MockitoExtension.class)
class ReinicioMonitorServiceTest {

    @Mock MonitoreoEstadoRepository estadoRepo;
    @Mock IncidenteService incidenteService;
    @InjectMocks ReinicioMonitorService service;

    private static final long UPTIME_5MIN = 300;

    @Test
    @DisplayName("primera vez (sin estado): fija la linea base y NO abre incidente")
    void primeraVezSinEstado() {
        when(estadoRepo.findById(1L)).thenReturn(Optional.empty());

        Optional<Incidente> r = service.procesarBootTime(LocalDateTime.now().minusSeconds(UPTIME_5MIN), UPTIME_5MIN);

        assertThat(r).isEmpty();
        verify(estadoRepo).save(any(MonitoreoEstado.class));      // guarda baseline
        verify(incidenteService, never()).crearDesdeMonitoreo(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("mismo arranque (boot time casi igual): NO abre incidente")
    void mismoArranqueNoDispara() {
        LocalDateTime boot = LocalDateTime.now().minusSeconds(UPTIME_5MIN);
        // Estado con el mismo boot time (diferencia de segundos por latencia, < tolerancia).
        when(estadoRepo.findById(1L)).thenReturn(Optional.of(
                MonitoreoEstado.builder().id(1L).ultimoArranque(boot.plusSeconds(20)).build()));

        Optional<Incidente> r = service.procesarBootTime(boot, UPTIME_5MIN);

        assertThat(r).isEmpty();
        verify(incidenteService, never()).crearDesdeMonitoreo(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("boot time nuevo (el host se reinicio): abre incidente y actualiza el estado")
    void reinicioDetectadoAbreIncidente() {
        LocalDateTime arranqueViejo = LocalDateTime.now().minusDays(3);   // llevaba 3 dias encendido
        LocalDateTime arranqueNuevo = LocalDateTime.now().minusSeconds(UPTIME_5MIN);  // recien reinicio
        when(estadoRepo.findById(1L)).thenReturn(Optional.of(
                MonitoreoEstado.builder().id(1L).ultimoArranque(arranqueViejo).build()));
        when(incidenteService.crearDesdeMonitoreo(any(), any(), eq(CategoriaIncidente.infraestructura),
                eq(Nivel.alto), eq(Nivel.alto)))
                .thenReturn(Incidente.builder().codigo("INC-2026-050").build());

        Optional<Incidente> r = service.procesarBootTime(arranqueNuevo, UPTIME_5MIN);

        assertThat(r).isPresent();
        assertThat(r.get().getCodigo()).isEqualTo("INC-2026-050");
        verify(incidenteService).crearDesdeMonitoreo(any(), any(), eq(CategoriaIncidente.infraestructura),
                eq(Nivel.alto), eq(Nivel.alto));
        verify(estadoRepo).save(any(MonitoreoEstado.class));    // actualiza el arranque conocido
    }
}
