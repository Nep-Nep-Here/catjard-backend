package com.catjard.inventory.service;

import com.catjard.inventory.client.CatalogClient;
import com.catjard.inventory.dto.CrearMovimientoDTO;
import com.catjard.inventory.dto.MovimientoDTO;
import com.catjard.inventory.model.Movimiento;
import com.catjard.inventory.repository.MovimientoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios para MovimientoService.
 * Valida que cada tipo de movimiento dispare el ajuste de stock correcto en
 * catalog-service via Feign (entrada suma, salida resta, ajuste respeta signo).
 */
@ExtendWith(MockitoExtension.class)
class MovimientoServiceTest {

    @Mock MovimientoRepository repo;
    @Mock CatalogClient catalogClient;

    @InjectMocks MovimientoService service;

    @Test
    @DisplayName("entrada: guarda movimiento y suma stock (delta positivo)")
    void crearEntradaSumaStock() {
        var dto = new CrearMovimientoDTO(
                "entrada", 1L, 100, "Compra a proveedor",
                "OC-2026-0017", "Marta Salinas", "Lote ABC"
        );
        when(repo.save(any(Movimiento.class))).thenAnswer(inv -> {
            Movimiento m = inv.getArgument(0);
            m.setId(1L);
            return m;
        });

        MovimientoDTO out = service.crear(dto);

        assertThat(out.tipo()).isEqualTo("entrada");
        assertThat(out.cantidad()).isEqualTo(100);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Integer>> body =
                (ArgumentCaptor<Map<String, Integer>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(Map.class);
        verify(catalogClient).actualizarStock(eq(1L), body.capture());
        assertThat(body.getValue()).containsEntry("delta", 100);
    }

    @Test
    @DisplayName("salida: guarda movimiento y resta stock (delta negativo)")
    void crearSalidaRestaStock() {
        var dto = new CrearMovimientoDTO(
                "salida", 8L, 50, "Salida a produccion",
                "PED-2026-0089", "Marta Salinas", null
        );
        when(repo.save(any(Movimiento.class))).thenAnswer(inv -> inv.getArgument(0));

        service.crear(dto);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Integer>> body =
                (ArgumentCaptor<Map<String, Integer>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(Map.class);
        verify(catalogClient).actualizarStock(eq(8L), body.capture());
        assertThat(body.getValue()).containsEntry("delta", -50);
    }

    @Test
    @DisplayName("ajuste: respeta el signo del delta enviado (puede ser negativo)")
    void crearAjusteRespetaSigno() {
        var dto = new CrearMovimientoDTO(
                "ajuste", 14L, -25, "Ajuste negativo",
                null, "Marta Salinas", "Diferencia inventario fisico"
        );
        when(repo.save(any(Movimiento.class))).thenAnswer(inv -> inv.getArgument(0));

        service.crear(dto);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Integer>> body =
                (ArgumentCaptor<Map<String, Integer>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(Map.class);
        verify(catalogClient).actualizarStock(eq(14L), body.capture());
        assertThat(body.getValue()).containsEntry("delta", -25);
    }

    @Test
    @DisplayName("tipo invalido: IllegalArgumentException antes de tocar BD o Feign")
    void tipoInvalidoLanzaExcepcion() {
        var dto = new CrearMovimientoDTO(
                "ROBO", 1L, 10, "Pruebita",
                null, null, null
        );

        assertThatThrownBy(() -> service.crear(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tipo de movimiento invalido");

        verify(repo, never()).save(any());
        verify(catalogClient, never()).actualizarStock(any(), any());
    }
}
