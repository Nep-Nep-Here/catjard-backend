package com.catjard.catalog.service;

import com.catjard.catalog.dto.ActualizarPromocionDTO;
import com.catjard.catalog.dto.CrearPromocionDTO;
import com.catjard.catalog.dto.PromocionDTO;
import com.catjard.catalog.mapper.PromocionMapper;
import com.catjard.catalog.model.AplicaA;
import com.catjard.catalog.model.Promocion;
import com.catjard.catalog.repository.PromocionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromocionService {

    private final PromocionRepository repo;
    private final PromocionMapper mapper;

    public List<PromocionDTO> listar() {
        return repo.findAll().stream().map(mapper::toDTO).toList();
    }

    public List<PromocionDTO> listarActivas() {
        return repo.findActivasVigentes(LocalDate.now()).stream().map(mapper::toDTO).toList();
    }

    public PromocionDTO obtener(Long id) {
        return repo.findById(id).map(mapper::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Promocion no encontrada: " + id));
    }

    @Transactional
    public PromocionDTO crear(CrearPromocionDTO dto) {
        validarPolimorfismo(dto.aplicaA(), dto.categoriaAplica(), dto.productoId());
        return mapper.toDTO(repo.save(mapper.fromCrear(dto)));
    }

    @Transactional
    public PromocionDTO actualizar(Long id, ActualizarPromocionDTO dto) {
        Promocion p = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Promocion no encontrada: " + id));
        if (dto.nombre() != null) p.setNombre(dto.nombre());
        if (dto.descripcion() != null) p.setDescripcion(dto.descripcion());
        if (dto.descuentoPct() != null) p.setDescuentoPct(dto.descuentoPct());
        if (dto.desde() != null) p.setDesde(dto.desde());
        if (dto.hasta() != null) p.setHasta(dto.hasta());
        if (dto.aplicaA() != null) {
            p.setAplicaA(dto.aplicaA());
            p.setCategoriaAplica(dto.aplicaA() == AplicaA.categoria ? dto.categoriaAplica() : null);
            p.setProductoId(dto.aplicaA() == AplicaA.producto ? dto.productoId() : null);
            validarPolimorfismo(p.getAplicaA(), p.getCategoriaAplica(), p.getProductoId());
        }
        if (dto.activa() != null) p.setActiva(dto.activa());
        return mapper.toDTO(p);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!repo.existsById(id)) {
            throw new IllegalArgumentException("Promocion no encontrada: " + id);
        }
        repo.deleteById(id);
    }

    private void validarPolimorfismo(AplicaA aplicaA, Object categoria, Long productoId) {
        switch (aplicaA) {
            case todo -> {
                if (categoria != null || productoId != null) {
                    throw new IllegalArgumentException("aplicaA=todo no debe llevar categoriaAplica ni productoId.");
                }
            }
            case categoria -> {
                if (categoria == null || productoId != null) {
                    throw new IllegalArgumentException("aplicaA=categoria requiere categoriaAplica y NO productoId.");
                }
            }
            case producto -> {
                if (productoId == null || categoria != null) {
                    throw new IllegalArgumentException("aplicaA=producto requiere productoId y NO categoriaAplica.");
                }
            }
        }
    }
}
