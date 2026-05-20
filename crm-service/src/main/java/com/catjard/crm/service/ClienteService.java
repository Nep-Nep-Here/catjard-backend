package com.catjard.crm.service;

import com.catjard.crm.dto.ActualizarClienteDTO;
import com.catjard.crm.dto.ClienteDTO;
import com.catjard.crm.dto.CrearClienteDTO;
import com.catjard.crm.mapper.ClienteMapper;
import com.catjard.crm.model.ClienteCRM;
import com.catjard.crm.repository.ClienteCRMRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteCRMRepository repo;

    @Transactional(readOnly = true)
    public List<ClienteDTO> listar(Optional<Boolean> activos) {
        List<ClienteCRM> data = activos.map(repo::findByCuentaActiva)
                .orElseGet(repo::findAllByOrderByRazonSocialAsc);
        return data.stream().map(ClienteMapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public ClienteDTO obtener(Long id) {
        return repo.findById(id).map(ClienteMapper::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public ClienteDTO obtenerPorRuc(String ruc) {
        return repo.findByRuc(ruc).map(ClienteMapper::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con RUC: " + ruc));
    }

    @Transactional
    public ClienteDTO crear(CrearClienteDTO dto) {
        if (repo.existsByRuc(dto.ruc())) {
            throw new IllegalStateException("Ya existe un cliente con RUC " + dto.ruc());
        }
        ClienteCRM cliente = ClienteMapper.fromCrear(dto);
        cliente.setFechaAlta(LocalDate.now());
        return ClienteMapper.toDTO(repo.save(cliente));
    }

    // Auto-registro de cliente: si ya existe un ClienteCRM con ese RUC se reutiliza
    // (mismo cliente B2B), si no se crea. Idempotente por RUC: evita duplicar empresas
    // cuando varios usuarios de la misma empresa se registran.
    @Transactional
    public ClienteDTO registrarOReutilizar(CrearClienteDTO dto) {
        return repo.findByRuc(dto.ruc())
                .map(ClienteMapper::toDTO)
                .orElseGet(() -> {
                    ClienteCRM cliente = ClienteMapper.fromCrear(dto);
                    cliente.setFechaAlta(LocalDate.now());
                    return ClienteMapper.toDTO(repo.save(cliente));
                });
    }

    @Transactional
    public ClienteDTO actualizar(Long id, ActualizarClienteDTO dto) {
        ClienteCRM c = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado: " + id));

        if (dto.razonSocial() != null)       c.setRazonSocial(dto.razonSocial());
        if (dto.nombreComercial() != null)   c.setNombreComercial(dto.nombreComercial());
        if (dto.industria() != null)         c.setIndustria(dto.industria());
        if (dto.contactoPrincipal() != null) c.setContactoPrincipal(dto.contactoPrincipal());
        if (dto.email() != null)             c.setEmail(dto.email());
        if (dto.telefono() != null)          c.setTelefono(dto.telefono());
        if (dto.direccion() != null)         c.setDireccion(dto.direccion());
        if (dto.cuentaActiva() != null)      c.setCuentaActiva(dto.cuentaActiva());
        if (dto.notas() != null)             c.setNotas(dto.notas());

        return ClienteMapper.toDTO(c);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!repo.existsById(id)) {
            throw new IllegalArgumentException("Cliente no encontrado: " + id);
        }
        repo.deleteById(id);
    }
}
