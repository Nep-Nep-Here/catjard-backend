package com.catjard.catalog.repository;

import com.catjard.catalog.model.Categoria;
import com.catjard.catalog.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<Producto> findByCategoria(Categoria categoria);
}
