package com.catjard.crm.service;

import com.catjard.crm.dto.ActualizarLeadDTO;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LeadService {

    private final LeadRepository leadRepo;
    private final ClienteCRMRepository clienteRepo;

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
    public Long convertirEnCliente(Long leadId, ConvertirLeadDTO dto) {
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
        return saved.getId();
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
