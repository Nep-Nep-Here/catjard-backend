package com.catjard.operations.service;

import com.catjard.operations.client.SalesClient;
import com.catjard.operations.dto.ArteDTO;
import com.catjard.operations.dto.RevisionArteDTO;
import com.catjard.operations.dto.SubirArteDTO;
import com.catjard.operations.model.Arte;
import com.catjard.operations.model.EstadoArte;
import com.catjard.operations.repository.ArteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
 * Tests unitarios para ArteService.
 * Valida el versionado de artes y la maquina de estados (en_revision -> aprobado /
 * rechazado), incluyendo el disparo de hitos de tracking y la sincronizacion del
 * estado del pedido en sales-service via Feign.
 */
@ExtendWith(MockitoExtension.class)
class ArteServiceTest {

    @Mock ArteRepository repo;
    @Mock TrackingService trackingService;
    @Mock SalesClient salesClient;

    @InjectMocks ArteService service;

    @Test
    @DisplayName("subir v1: crea version 1 en_revision, registra hito y avisa a sales (en_diseno)")
    void subirPrimeraVersion() {
        when(repo.findFirstByPedidoCodigoOrderByVersionDesc("PED-2026-0001"))
                .thenReturn(Optional.empty());
        when(repo.save(any(Arte.class))).thenAnswer(inv -> {
            Arte a = inv.getArgument(0);
            a.setId(1L);
            return a;
        });

        ArteDTO out = service.subirNuevaVersion("PED-2026-0001", new SubirArteDTO("arte-v1.pdf"));

        assertThat(out.version()).isEqualTo(1);
        assertThat(out.estado()).isEqualTo("en_revision");
        verify(trackingService).registrarHitoSilencioso(eq("PED-2026-0001"), any(), any(), anyString());
        verify(salesClient).actualizarPedidoPorCodigo(eq("PED-2026-0001"), any());
    }

    @Test
    @DisplayName("subir v2+: incrementa version y NO repite hito ni aviso a sales")
    void subirVersionSiguiente() {
        Arte previa = Arte.builder()
                .id(5L).pedidoCodigo("PED-2026-0001").version(2)
                .nombreArchivo("arte-v2.pdf").estado(EstadoArte.rechazado)
                .build();
        when(repo.findFirstByPedidoCodigoOrderByVersionDesc("PED-2026-0001"))
                .thenReturn(Optional.of(previa));
        when(repo.save(any(Arte.class))).thenAnswer(inv -> inv.getArgument(0));

        ArteDTO out = service.subirNuevaVersion("PED-2026-0001", new SubirArteDTO("arte-v3.pdf"));

        assertThat(out.version()).isEqualTo(3);
        verify(trackingService, never()).registrarHitoSilencioso(any(), any(), any(), any());
        verify(salesClient, never()).actualizarPedidoPorCodigo(any(), any());
    }

    @Test
    @DisplayName("aprobar OK: pasa a aprobado, guarda comentarios y manda pedido a produccion")
    void aprobarOk() {
        Arte arte = Arte.builder()
                .id(7L).pedidoCodigo("PED-2026-0002").version(1)
                .nombreArchivo("arte.pdf").estado(EstadoArte.en_revision)
                .build();
        when(repo.findById(7L)).thenReturn(Optional.of(arte));

        ArteDTO out = service.aprobar(7L, new RevisionArteDTO("Aprobado por el cliente"));

        assertThat(out.estado()).isEqualTo("aprobado");
        assertThat(arte.getComentariosCliente()).isEqualTo("Aprobado por el cliente");
        verify(trackingService).registrarHitoSilencioso(eq("PED-2026-0002"), any(), any(), anyString());
        verify(salesClient).actualizarPedidoPorCodigo(eq("PED-2026-0002"), any());
    }

    @Test
    @DisplayName("aprobar KO: IllegalArgumentException cuando el arte no existe")
    void aprobarArteNoEncontrado() {
        when(repo.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.aprobar(404L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Arte no encontrado");

        verify(salesClient, never()).actualizarPedidoPorCodigo(any(), any());
    }

    @Test
    @DisplayName("rechazar OK: pasa a rechazado y NO toca tracking ni sales")
    void rechazarOk() {
        Arte arte = Arte.builder()
                .id(8L).pedidoCodigo("PED-2026-0003").version(2)
                .nombreArchivo("arte.pdf").estado(EstadoArte.en_revision)
                .build();
        when(repo.findById(8L)).thenReturn(Optional.of(arte));

        ArteDTO out = service.rechazar(8L, new RevisionArteDTO("Cambiar el logo"));

        assertThat(out.estado()).isEqualTo("rechazado");
        assertThat(arte.getComentariosCliente()).isEqualTo("Cambiar el logo");
        verify(trackingService, never()).registrarHitoSilencioso(any(), any(), any(), any());
        verify(salesClient, never()).actualizarPedidoPorCodigo(any(), any());
    }
}
