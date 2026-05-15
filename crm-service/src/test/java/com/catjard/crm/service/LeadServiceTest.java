package com.catjard.crm.service;

import com.catjard.crm.client.IdentityClient;
import com.catjard.crm.dto.ConversionResultDTO;
import com.catjard.crm.dto.ConvertirLeadDTO;
import com.catjard.crm.dto.CrearLeadDTO;
import com.catjard.crm.dto.LeadDTO;
import com.catjard.crm.model.ClienteCRM;
import com.catjard.crm.model.EstadoLead;
import com.catjard.crm.model.Lead;
import com.catjard.crm.repository.ClienteCRMRepository;
import com.catjard.crm.repository.LeadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios para LeadService.
 * Cubre el alta de leads (codigo autogenerado) y la conversion lead -> cliente CRM,
 * incluyendo la creacion de cuenta de acceso via Feign (identity-service) y sus fallos.
 */
@ExtendWith(MockitoExtension.class)
class LeadServiceTest {

    @Mock LeadRepository leadRepo;
    @Mock ClienteCRMRepository clienteRepo;
    @Mock IdentityClient identityClient;

    @InjectMocks LeadService service;

    private Lead leadMock;

    @BeforeEach
    void setUp() {
        leadMock = Lead.builder()
                .id(1L)
                .codigo("LEAD-2026-0001")
                .nombre("Lucia Montoya")
                .email("lucia@empresa.com")
                .telefono("+51 999111222")
                .estado(EstadoLead.nuevo)
                .build();
    }

    @Test
    @DisplayName("crear OK: genera codigo correlativo, estado nuevo y persiste el lead")
    void crearOk() {
        var dto = new CrearLeadDTO(
                "Lucia Montoya", "Banco Sigma", "20512345671",
                "lucia@empresa.com", "+51 999111222", "Polos", "300", "Cotizar"
        );
        when(leadRepo.findByCodigoStartingWithOrderByCodigoDesc(anyString())).thenReturn(List.of());
        when(leadRepo.save(any(Lead.class))).thenAnswer(inv -> {
            Lead l = inv.getArgument(0);
            l.setId(10L);
            return l;
        });

        LeadDTO out = service.crear(dto);

        assertThat(out.nombre()).isEqualTo("Lucia Montoya");
        assertThat(out.estado()).isEqualTo("nuevo");
        assertThat(out.codigo()).matches("LEAD-\\d{4}-0001");
        verify(leadRepo).save(any(Lead.class));
    }

    @Test
    @DisplayName("obtener KO: IllegalArgumentException cuando el lead no existe")
    void obtenerNoEncontrado() {
        when(leadRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtener(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Lead no encontrado");
    }

    @Test
    @DisplayName("convertir OK: crea cliente, marca lead convertido y crea cuenta de acceso")
    void convertirOk() {
        var dto = new ConvertirLeadDTO(
                "Banco Sigma S.A.", "Banco Sigma", "20512345671",
                "Banca", "Av. Demo 123", "Cliente preferente"
        );
        when(leadRepo.findById(1L)).thenReturn(Optional.of(leadMock));
        when(clienteRepo.existsByRuc("20512345671")).thenReturn(false);
        when(clienteRepo.save(any(ClienteCRM.class))).thenAnswer(inv -> {
            ClienteCRM c = inv.getArgument(0);
            c.setId(50L);
            return c;
        });
        when(identityClient.crearClienteDesdeLead(anyMap())).thenReturn(Map.of("id", 7));

        ConversionResultDTO res = service.convertirEnCliente(1L, dto);

        assertThat(res.clienteId()).isEqualTo(50L);
        assertThat(res.cuentaCreada()).isTrue();
        assertThat(res.passwordTemporal()).isNotBlank();
        assertThat(leadMock.getEstado()).isEqualTo(EstadoLead.convertido);
        verify(identityClient).crearClienteDesdeLead(anyMap());
    }

    @Test
    @DisplayName("convertir KO: IllegalStateException si el lead ya fue convertido")
    void convertirLeadYaConvertido() {
        leadMock.setEstado(EstadoLead.convertido);
        when(leadRepo.findById(1L)).thenReturn(Optional.of(leadMock));

        var dto = new ConvertirLeadDTO("Razon", null, "20512345671", null, null, null);

        assertThatThrownBy(() -> service.convertirEnCliente(1L, dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ya fue convertido");

        verify(clienteRepo, never()).save(any());
        verify(identityClient, never()).crearClienteDesdeLead(any());
    }

    @Test
    @DisplayName("convertir KO: IllegalStateException si ya existe cliente con ese RUC")
    void convertirRucDuplicado() {
        when(leadRepo.findById(1L)).thenReturn(Optional.of(leadMock));
        when(clienteRepo.existsByRuc("20512345671")).thenReturn(true);

        var dto = new ConvertirLeadDTO("Razon", null, "20512345671", null, null, null);

        assertThatThrownBy(() -> service.convertirEnCliente(1L, dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Ya existe un cliente con RUC");

        verify(clienteRepo, never()).save(any());
        verify(identityClient, never()).crearClienteDesdeLead(any());
    }

    @Test
    @DisplayName("convertir degradado: si identity-service falla, cliente se crea pero sin cuenta")
    void convertirSinCuentaCuandoIdentityFalla() {
        var dto = new ConvertirLeadDTO(
                "Banco Sigma S.A.", "Banco Sigma", "20512345671",
                "Banca", "Av. Demo 123", null
        );
        when(leadRepo.findById(1L)).thenReturn(Optional.of(leadMock));
        when(clienteRepo.existsByRuc("20512345671")).thenReturn(false);
        when(clienteRepo.save(any(ClienteCRM.class))).thenAnswer(inv -> {
            ClienteCRM c = inv.getArgument(0);
            c.setId(50L);
            return c;
        });
        when(identityClient.crearClienteDesdeLead(anyMap()))
                .thenThrow(new RuntimeException("identity-service caido"));

        ConversionResultDTO res = service.convertirEnCliente(1L, dto);

        assertThat(res.cuentaCreada()).isFalse();
        assertThat(res.passwordTemporal()).isNull();
        assertThat(res.mensaje()).contains("no se pudo crear su cuenta");
        assertThat(leadMock.getEstado()).isEqualTo(EstadoLead.convertido);
        verify(clienteRepo).save(any(ClienteCRM.class));
    }
}
