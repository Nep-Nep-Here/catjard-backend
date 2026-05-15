package com.catjard.catalog.service;

import com.catjard.catalog.dto.ActualizarProductoDTO;
import com.catjard.catalog.dto.ActualizarStockDTO;
import com.catjard.catalog.dto.CrearProductoDTO;
import com.catjard.catalog.dto.ProductoDTO;
import com.catjard.catalog.mapper.ProductoMapper;
import com.catjard.catalog.model.Categoria;
import com.catjard.catalog.model.Producto;
import com.catjard.catalog.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProductoService {

    public static final List<String> TECNICAS_VALIDAS = List.of(
            "Serigrafía", "Sublimado", "DTF", "Bordado", "Grabado Láser", "Impresión UV"
    );

    private final ProductoRepository repo;
    private final ProductoMapper mapper;

    public List<ProductoDTO> listar(Categoria categoria, String q) {
        List<Producto> base = (categoria != null) ? repo.findByCategoria(categoria) : repo.findAll();
        if (q != null && !q.isBlank()) {
            String needle = q.toLowerCase();
            base = base.stream()
                    .filter(p -> p.getNombre().toLowerCase().contains(needle)
                              || p.getSlug().toLowerCase().contains(needle))
                    .toList();
        }
        return base.stream().map(mapper::toDTO).toList();
    }

    public ProductoDTO obtener(Long id) {
        return repo.findById(id).map(mapper::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + id));
    }

    public ProductoDTO obtenerPorSlug(String slug) {
        return repo.findBySlug(slug).map(mapper::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + slug));
    }

    @Transactional
    public ProductoDTO crear(CrearProductoDTO dto) {
        validarTecnicas(dto.tecnicas());
        if (repo.existsBySlug(dto.slug())) {
            throw new IllegalArgumentException("Ya existe un producto con ese slug.");
        }
        return mapper.toDTO(repo.save(mapper.fromCrear(dto)));
    }

    @Transactional
    public ProductoDTO actualizar(Long id, ActualizarProductoDTO dto) {
        Producto p = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + id));
        if (dto.slug() != null && !dto.slug().equals(p.getSlug())) {
            if (repo.existsBySlug(dto.slug())) {
                throw new IllegalArgumentException("Ya existe un producto con ese slug.");
            }
            p.setSlug(dto.slug());
        }
        if (dto.nombre() != null) p.setNombre(dto.nombre());
        if (dto.categoria() != null) p.setCategoria(dto.categoria());
        if (dto.precio() != null) p.setPrecio(dto.precio());
        if (dto.stock() != null) p.setStock(dto.stock());
        if (dto.stockMinimo() != null) p.setStockMinimo(dto.stockMinimo());
        if (dto.descripcion() != null) p.setDescripcion(dto.descripcion());
        if (dto.imagenUrl() != null) {
            p.setImagenUrl(dto.imagenUrl().isBlank() ? null : dto.imagenUrl());
        }
        if (dto.tecnicas() != null) {
            validarTecnicas(dto.tecnicas());
            p.setTecnicas(new HashSet<>(dto.tecnicas()));
        }
        return mapper.toDTO(p);
    }

    @Transactional
    public ProductoDTO actualizarStock(Long id, ActualizarStockDTO dto) {
        Producto p = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + id));
        if (dto.delta() != null) {
            int nuevo = p.getStock() + dto.delta();
            if (nuevo < 0) {
                throw new IllegalStateException("Stock insuficiente para producto " + id
                        + " (actual " + p.getStock() + ", delta " + dto.delta() + ")");
            }
            p.setStock(nuevo);
        } else if (dto.stock() != null) {
            p.setStock(dto.stock());
        } else {
            throw new IllegalArgumentException("Debe enviarse 'stock' (absoluto) o 'delta' (incremento)");
        }
        return mapper.toDTO(p);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!repo.existsById(id)) {
            throw new IllegalArgumentException("Producto no encontrado: " + id);
        }
        repo.deleteById(id);
    }

    private void validarTecnicas(Set<String> tecnicas) {
        for (String t : tecnicas) {
            if (!TECNICAS_VALIDAS.contains(t)) {
                throw new IllegalArgumentException("Tecnica no valida: " + t
                        + ". Permitidas: " + TECNICAS_VALIDAS);
            }
        }
    }
}
