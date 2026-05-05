package com.catjard.inventory.service;

import com.catjard.inventory.dto.ActualizarProveedorDTO;
import com.catjard.inventory.dto.CrearProveedorDTO;
import com.catjard.inventory.dto.ProveedorDTO;
import com.catjard.inventory.mapper.ProveedorMapper;
import com.catjard.inventory.model.Proveedor;
import com.catjard.inventory.repository.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProveedorService {

    private final ProveedorRepository repo;

    @Transactional(readOnly = true)
    public List<ProveedorDTO> listar(Optional<Boolean> activos) {
        List<Proveedor> data = activos.map(repo::findByActivo)
                .orElseGet(repo::findAllByOrderByRazonSocialAsc);
        return data.stream().map(ProveedorMapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public ProveedorDTO obtener(Long id) {
        return repo.findById(id).map(ProveedorMapper::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado: " + id));
    }

    @Transactional
    public ProveedorDTO crear(CrearProveedorDTO dto) {
        if (repo.existsByRuc(dto.ruc())) {
            throw new IllegalStateException("Ya existe un proveedor con RUC " + dto.ruc());
        }
        Proveedor p = ProveedorMapper.fromCrear(dto);
        p.setFechaAlta(LocalDate.now());
        return ProveedorMapper.toDTO(repo.save(p));
    }

    @Transactional
    public ProveedorDTO actualizar(Long id, ActualizarProveedorDTO dto) {
        Proveedor p = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado: " + id));

        if (dto.razonSocial() != null)     p.setRazonSocial(dto.razonSocial());
        if (dto.nombreComercial() != null) p.setNombreComercial(dto.nombreComercial());
        if (dto.contacto() != null)        p.setContacto(dto.contacto());
        if (dto.email() != null)           p.setEmail(dto.email());
        if (dto.telefono() != null)        p.setTelefono(dto.telefono());
        if (dto.direccion() != null)       p.setDireccion(dto.direccion());
        if (dto.productos() != null)       p.setProductos(dto.productos());
        if (dto.notas() != null)           p.setNotas(dto.notas());
        if (dto.activo() != null)          p.setActivo(dto.activo());

        return ProveedorMapper.toDTO(p);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!repo.existsById(id)) {
            throw new IllegalArgumentException("Proveedor no encontrado: " + id);
        }
        repo.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Proveedor referencia(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado: " + id));
    }
}
