package com.catjard.solicitudes.service;

import com.catjard.solicitudes.digitalocean.DigitalOceanService;
import com.catjard.solicitudes.dto.ActualizarServicioCriticoDTO;
import com.catjard.solicitudes.dto.ResumenContinuidadDTO;
import com.catjard.solicitudes.model.CriticidadServicio;
import com.catjard.solicitudes.model.EstadoIncidente;
import com.catjard.solicitudes.model.Incidente;
import com.catjard.solicitudes.model.ServicioCritico;
import com.catjard.solicitudes.model.TipoServicio;
import com.catjard.solicitudes.repository.IncidenteRepository;
import com.catjard.solicitudes.repository.RespaldoRepository;
import com.catjard.solicitudes.repository.RiesgoRepository;
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
 * Metricas de cumplimiento RTO del tablero de continuidad.
 * Un incidente cancelado es un falso positivo: no computa ni como cumplido, ni como
 * incumplido, ni como "vencido activo" (no hay servicio caido que recuperar).
 */
@ExtendWith(MockitoExtension.class)
class ContinuidadResumenRtoTest {

    @Mock ServicioCriticoRepository servicioRepo;
    @Mock RiesgoRepository riesgoRepo;
    @Mock RespaldoRepository respaldoRepo;
    @Mock IncidenteRepository incidenteRepo;
    @Mock DigitalOceanService digitalOcean;
    @InjectMocks ContinuidadService service;

    // Incidente con deadline RTO ya vencido (nunca resuelto).
    private Incidente vencidoSinResolver(EstadoIncidente estado) {
        return Incidente.builder()
                .id(1L).codigo("INC-2026-001").estado(estado)
                .servicioId(9L).rtoMinutos(120)
                .rtoDeadline(LocalDateTime.now().minusHours(3))
                .build();
    }

    private ResumenContinuidadDTO resumenCon(List<Incidente> incidentes) {
        when(servicioRepo.findAllByOrderByPrioridadRecuperacionAscIdAsc()).thenReturn(List.of());
        when(incidenteRepo.findByRtoDeadlineIsNotNull()).thenReturn(incidentes);
        return service.resumen();
    }

    @Test
    @DisplayName("un incidente abierto con el plazo pasado cuenta como RTO vencido activo")
    void abiertoVencidoCuenta() {
        var r = resumenCon(List.of(vencidoSinResolver(EstadoIncidente.registrado)));

        assertThat(r.incidentesActivosRtoVencido()).isEqualTo(1);
    }

    @Test
    @DisplayName("un incidente cancelado con el plazo pasado NO cuenta como RTO vencido activo")
    void canceladoVencidoNoCuenta() {
        var r = resumenCon(List.of(vencidoSinResolver(EstadoIncidente.cancelado)));

        assertThat(r.incidentesActivosRtoVencido()).isZero();
    }

    @Test
    @DisplayName("un cancelado no ensucia el porcentaje de cumplimiento (queda fuera de la medicion)")
    void canceladoNoEntraEnElPorcentaje() {
        Incidente cumplido = vencidoSinResolver(EstadoIncidente.cerrado);
        cumplido.setCumplioRto(true);

        var r = resumenCon(List.of(cumplido, vencidoSinResolver(EstadoIncidente.cancelado)));

        assertThat(r.incidentesRtoCumplido()).isEqualTo(1);
        assertThat(r.incidentesRtoIncumplido()).isZero();
        assertThat(r.porcentajeCumplimientoRto()).isEqualTo(100.0);  // el cancelado no lo baja
    }

    // Un servicio del catalogo con RPO definido (aparece en el semaforo).
    private ServicioCritico servicioConRpo(Integer rpo) {
        return ServicioCritico.builder()
                .id(5L).codigo("SRV-003").nombre("Identity Service")
                .tipo(TipoServicio.microservicio).criticidad(CriticidadServicio.critica)
                .prioridadRecuperacion(3).rtoMinutos(30).rpoMinutos(rpo).activo(true)
                .build();
    }

    private ActualizarServicioCriticoDTO soloRpo(Integer rpo) {
        return new ActualizarServicioCriticoDTO(null, null, null, null, null, null, rpo, null, null);
    }

    @Test
    @DisplayName("editar un servicio con RPO vacio le QUITA el objetivo (sale del semaforo)")
    void editarConRpoVacioLoQuita() {
        ServicioCritico s = servicioConRpo(1440);
        when(servicioRepo.findById(5L)).thenReturn(Optional.of(s));

        service.actualizarServicio(5L, soloRpo(null));

        assertThat(s.getRpoMinutos()).isNull();  // sin objetivo RPO -> el resumen ya no lo incluye
    }

    @Test
    @DisplayName("editar con un RPO nuevo lo actualiza")
    void editarConRpoNuevoLoActualiza() {
        ServicioCritico s = servicioConRpo(1440);
        when(servicioRepo.findById(5L)).thenReturn(Optional.of(s));

        service.actualizarServicio(5L, soloRpo(2880));

        assertThat(s.getRpoMinutos()).isEqualTo(2880);
    }
}
