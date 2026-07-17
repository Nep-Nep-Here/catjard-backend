package com.catjard.sales.service;

import com.catjard.sales.client.InventoryClient;
import com.catjard.sales.dto.ActualizarPedidoDTO;
import com.catjard.sales.model.EstadoPedido;
import com.catjard.sales.model.Pedido;
import com.catjard.sales.repository.PedidoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests para la traza de usuario del pedido (columna "Usuario" de Auditoria).
 * JwtAuthenticationFilter deja el nombre del usuario en `credentials`, y al
 * procesar el pedido (cambio de estado) ese nombre queda en `procesadoPor`.
 */
class PedidoProcesadoPorTest {

    private final PedidoRepository repo = mock(PedidoRepository.class);
    private final InventoryClient inventoryClient = mock(InventoryClient.class);
    private final PedidoService service = new PedidoService(repo, inventoryClient);

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarComo(String nombre) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user@catjard.com", nombre, List.of()));
    }

    private Pedido pedidoEn(EstadoPedido estado) {
        Pedido p = Pedido.builder().id(1L).codigo("PED-2026-0001").estado(estado).build();
        when(repo.findById(1L)).thenReturn(Optional.of(p));
        return p;
    }

    @Test
    @DisplayName("al cambiar de estado guarda el usuario logueado en procesadoPor")
    void guardaUsuarioAlProcesar() {
        Pedido p = pedidoEn(EstadoPedido.por_iniciar);
        autenticarComo("Carlos Rivas");

        var dto = service.actualizar(1L, new ActualizarPedidoDTO("en_diseno", null, null, null, null, null));

        assertThat(p.getProcesadoPor()).isEqualTo("Carlos Rivas");
        assertThat(dto.procesadoPor()).isEqualTo("Carlos Rivas");
    }

    @Test
    @DisplayName("cada cambio de estado reatribuye el pedido a quien lo movio")
    void reatribuyeAlSiguienteUsuario() {
        Pedido p = pedidoEn(EstadoPedido.por_iniciar);
        p.setProcesadoPor("Carlos Rivas");
        autenticarComo("Lucia Mendoza");

        service.actualizar(1L, new ActualizarPedidoDTO("en_produccion", null, null, null, null, null));

        assertThat(p.getProcesadoPor()).isEqualTo("Lucia Mendoza");
    }

    @Test
    @DisplayName("un PATCH que no cambia el estado no reescribe procesadoPor")
    void patchSinCambioDeEstadoNoReescribe() {
        Pedido p = pedidoEn(EstadoPedido.listo);
        p.setProcesadoPor("Carlos Rivas");
        autenticarComo("Lucia Mendoza");

        service.actualizar(1L, new ActualizarPedidoDTO(null, null, null, null, "Olva Courier", null));

        assertThat(p.getProcesadoPor()).isEqualTo("Carlos Rivas");
    }

    @Test
    @DisplayName("sin autenticacion (llamada interna) atribuye a 'sistema'")
    void sinAutenticacionAtribuyeASistema() {
        Pedido p = pedidoEn(EstadoPedido.por_iniciar);

        service.actualizar(1L, new ActualizarPedidoDTO("en_diseno", null, null, null, null, null));

        assertThat(p.getProcesadoPor()).isEqualTo("sistema");
    }
}
