package com.catjard.crm.service;

import com.catjard.crm.client.IdentityClient;
import com.catjard.crm.dto.ActualizarLeadDTO;
import com.catjard.crm.dto.ConversionResultDTO;
import com.catjard.crm.dto.ConvertirLeadDTO;
import com.catjard.crm.dto.CrearLeadDTO;
import com.catjard.crm.dto.LeadDTO;
import com.catjard.crm.mapper.LeadMapper;
import com.catjard.crm.model.ClienteCRM;
import com.catjard.crm.model.EstadoLead;
import com.catjard.crm.model.Lead;
import com.catjard.crm.repository.ClienteCRMRepository;
import com.catjard.crm.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeadService {

    private static final SecureRandom RNG = new SecureRandom();
    private static final String PASS_ALPHABET = "abcdefghjkmnpqrstuvwxyz23456789";

    private final LeadRepository leadRepo;
    private final ClienteCRMRepository clienteRepo;
    private final IdentityClient identityClient;

    @Transactional(readOnly = true)
    public List<LeadDTO> listar(Optional<String> estado) {
        List<Lead> leads;
        if (estado.isPresent() && !estado.get().isBlank()) {
            EstadoLead e = parseEstado(estado.get());
            leads = leadRepo.findByEstado(e);
        } else {
            leads = leadRepo.findAllByOrderByFechaDescIdDesc();
        }
        return leads.stream().map(LeadMapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public LeadDTO obtener(Long id) {
        return leadRepo.findById(id).map(LeadMapper::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Lead no encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public LeadDTO obtenerPorCodigo(String codigo) {
        return leadRepo.findByCodigo(codigo).map(LeadMapper::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Lead no encontrado: " + codigo));
    }

    @Transactional
    public LeadDTO crear(CrearLeadDTO dto) {
        Lead lead = LeadMapper.fromCrear(dto);
        lead.setFecha(LocalDate.now());
        lead.setCodigo(generarCodigo());
        return LeadMapper.toDTO(leadRepo.save(lead));
    }

    @Transactional
    public LeadDTO actualizar(Long id, ActualizarLeadDTO dto) {
        Lead lead = leadRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lead no encontrado: " + id));

        if (dto.nombre() != null)        lead.setNombre(dto.nombre());
        if (dto.empresa() != null)       lead.setEmpresa(dto.empresa());
        if (dto.ruc() != null)           lead.setRuc(dto.ruc());
        if (dto.email() != null)         lead.setEmail(dto.email());
        if (dto.telefono() != null)      lead.setTelefono(dto.telefono());
        if (dto.productos() != null)     lead.setProductos(dto.productos());
        if (dto.cantidad() != null)      lead.setCantidad(dto.cantidad());
        if (dto.mensaje() != null)       lead.setMensaje(dto.mensaje());
        if (dto.estado() != null)        lead.setEstado(parseEstado(dto.estado()));
        if (dto.asignadoA() != null)     lead.setAsignadoA(dto.asignadoA());
        if (dto.notasInternas() != null) lead.setNotasInternas(dto.notasInternas());

        return LeadMapper.toDTO(lead);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!leadRepo.existsById(id)) {
            throw new IllegalArgumentException("Lead no encontrado: " + id);
        }
        leadRepo.deleteById(id);
    }

    @Transactional
    public ConversionResultDTO convertirEnCliente(Long leadId, ConvertirLeadDTO dto) {
        Lead lead = leadRepo.findById(leadId)
                .orElseThrow(() -> new IllegalArgumentException("Lead no encontrado: " + leadId));

        if (lead.getEstado() == EstadoLead.convertido) {
            throw new IllegalStateException("Lead ya fue convertido");
        }
        if (clienteRepo.existsByRuc(dto.ruc())) {
            throw new IllegalStateException("Ya existe un cliente con RUC " + dto.ruc());
        }

        ClienteCRM cliente = ClienteCRM.builder()
                .razonSocial(dto.razonSocial())
                .nombreComercial(dto.nombreComercial())
                .ruc(dto.ruc())
                .industria(dto.industria())
                .contactoPrincipal(lead.getNombre())
                .email(lead.getEmail())
                .telefono(lead.getTelefono())
                .direccion(dto.direccion())
                .cuentaActiva(Boolean.TRUE)
                .fechaAlta(LocalDate.now())
                .notas(dto.notas())
                .build();

        ClienteCRM saved = clienteRepo.save(cliente);
        lead.setEstado(EstadoLead.convertido);

        // Crear usuario en identity-service para que el cliente pueda loguearse.
        String passwordTemporal = generarPasswordTemporal();
        boolean cuentaCreada = false;
        String mensaje;
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("email", lead.getEmail());
            body.put("password", passwordTemporal);
            body.put("nombre", lead.getNombre());
            body.put("empresa", dto.razonSocial());
            body.put("ruc", dto.ruc());
            body.put("telefono", lead.getTelefono());
            body.put("direccion", dto.direccion());
            body.put("clienteId", saved.getId());
            identityClient.crearClienteDesdeLead(body);
            cuentaCreada = true;
            mensaje = "Cuenta creada. Comparte estas credenciales con el cliente; la contraseña no se vuelve a mostrar.";
        } catch (Exception ex) {
            log.warn("No se pudo crear cuenta de acceso para lead {}: {}", leadId, ex.getMessage());
            passwordTemporal = null;
            mensaje = "Cliente CRM creado, pero no se pudo crear su cuenta de acceso automáticamente: "
                    + ex.getMessage() + ". Crea el usuario manualmente desde Gerencia → Usuarios.";
        }

        return new ConversionResultDTO(
                leadId,
                saved.getId(),
                lead.getEmail(),
                passwordTemporal,
                cuentaCreada,
                mensaje
        );
    }

    private String generarPasswordTemporal() {
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            sb.append(PASS_ALPHABET.charAt(RNG.nextInt(PASS_ALPHABET.length())));
            if (i == 3 || i == 6) sb.append('-');
        }
        return sb.toString();
    }

    private EstadoLead parseEstado(String s) {
        try {
            return EstadoLead.valueOf(s.toLowerCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Estado invalido: " + s);
        }
    }

    private String generarCodigo() {
        int year = LocalDate.now().getYear();
        String prefijo = "LEAD-" + year + "-";
        int siguiente = leadRepo.findByCodigoStartingWithOrderByCodigoDesc(prefijo).stream()
                .findFirst()
                .map(l -> {
                    String[] parts = l.getCodigo().split("-");
                    try { return Integer.parseInt(parts[2]) + 1; }
                    catch (Exception e) { return 1; }
                })
                .orElse(1);
        return prefijo + String.format("%04d", siguiente);
    }
}
