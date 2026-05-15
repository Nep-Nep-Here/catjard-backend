package com.catjard.catalog.service;

import com.catjard.catalog.dto.ActualizarStockDTO;
import com.catjard.catalog.dto.CrearProductoDTO;
import com.catjard.catalog.dto.ProductoDTO;
import com.catjard.catalog.mapper.ProductoMapper;
import com.catjard.catalog.model.Categoria;
import com.catjard.catalog.model.Producto;
import com.catjard.catalog.repository.ProductoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios para ProductoService.
 * Cubre el filtrado del catalogo, busqueda por slug, el ajuste de stock
 * (absoluto / por delta, con control de stock negativo) y validaciones de alta.
 */
@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock ProductoRepository repo;
    @Mock ProductoMapper mapper;

    @InjectMocks ProductoService service;

    private Producto producto(Long id, String slug, String nombre, int stock) {
        return Producto.builder()
                .id(id).slug(slug).nombre(nombre)
                .categoria(Categoria.bebidas)
                .precio(new BigDecimal("25.00"))
                .stock(stock).stockMinimo(5)
                .build();
    }

    private ProductoDTO dtoDe(Producto p) {
        return new ProductoDTO(p.getId(), p.getSlug(), p.getNombre(), "bebidas",
                p.getPrecio(), p.getStock(), p.getStockMinimo(), null, null, List.of());
    }

    @Test
    @DisplayName("listar con q: filtra por nombre/slug que contengan el termino")
    void listarFiltraPorTexto() {
        Producto termo = producto(1L, "termo-acero", "Termo Acero", 40);
        Producto polo = producto(2L, "polo-pique", "Polo Pique", 100);
        when(repo.findAll()).thenReturn(List.of(termo, polo));
        when(mapper.toDTO(termo)).thenReturn(dtoDe(termo));

        List<ProductoDTO> out = service.listar(null, "termo");

        assertThat(out).hasSize(1);
        assertThat(out.get(0).slug()).isEqualTo("termo-acero");
    }

    @Test
    @DisplayName("listar por categoria: usa findByCategoria y no findAll")
    void listarPorCategoria() {
        Producto termo = producto(1L, "termo-acero", "Termo Acero", 40);
        when(repo.findByCategoria(Categoria.bebidas)).thenReturn(List.of(termo));
        when(mapper.toDTO(termo)).thenReturn(dtoDe(termo));

        List<ProductoDTO> out = service.listar(Categoria.bebidas, null);

        assertThat(out).hasSize(1);
        verify(repo).findByCategoria(Categoria.bebidas);
        verify(repo, never()).findAll();
    }

    @Test
    @DisplayName("obtenerPorSlug KO: IllegalArgumentException cuando no existe")
    void obtenerPorSlugNoEncontrado() {
        when(repo.findBySlug("inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerPorSlug("inexistente"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Producto no encontrado");
    }

    @Test
    @DisplayName("crear KO: tecnica no valida lanza excepcion antes de tocar la BD")
    void crearTecnicaInvalida() {
        var dto = new CrearProductoDTO(
                "gorra-x", "Gorra X", Categoria.vestimenta, new BigDecimal("15.00"),
                10, 2, "desc", null, Set.of("Hechizo")
        );

        assertThatThrownBy(() -> service.crear(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tecnica no valida");

        verify(repo, never()).existsBySlug(any());
        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("crear KO: slug duplicado lanza excepcion y no persiste")
    void crearSlugDuplicado() {
        var dto = new CrearProductoDTO(
                "termo-acero", "Termo Acero", Categoria.bebidas, new BigDecimal("25.00"),
                40, 5, "desc", null, Set.of("DTF")
        );
        when(repo.existsBySlug("termo-acero")).thenReturn(true);

        assertThatThrownBy(() -> service.crear(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe un producto con ese slug");

        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("actualizarStock por delta: suma correctamente al stock actual")
    void actualizarStockDeltaPositivo() {
        Producto p = producto(1L, "termo-acero", "Termo Acero", 5);
        when(repo.findById(1L)).thenReturn(Optional.of(p));
        when(mapper.toDTO(p)).thenReturn(dtoDe(p));

        service.actualizarStock(1L, new ActualizarStockDTO(null, 3));

        assertThat(p.getStock()).isEqualTo(8);
        verify(mapper).toDTO(p);
    }

    @Test
    @DisplayName("actualizarStock KO: delta que deja stock negativo lanza IllegalStateException")
    void actualizarStockDeltaInsuficiente() {
        Producto p = producto(1L, "termo-acero", "Termo Acero", 5);
        when(repo.findById(1L)).thenReturn(Optional.of(p));

        assertThatThrownBy(() -> service.actualizarStock(1L, new ActualizarStockDTO(null, -10)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Stock insuficiente");

        assertThat(p.getStock()).isEqualTo(5);
        verify(mapper, never()).toDTO(any());
    }

    @Test
    @DisplayName("eliminar KO: IllegalArgumentException si el producto no existe")
    void eliminarNoEncontrado() {
        when(repo.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.eliminar(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Producto no encontrado");

        verify(repo, never()).deleteById(any());
    }
}
