package com.catjard.catalog.controller;

import com.catjard.catalog.dto.CategoriaDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Categorias", description = "Catalogo fijo de categorias")
@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private static final List<CategoriaDTO> CATEGORIAS = List.of(
            new CategoriaDTO("vestimenta", "Vestimenta"),
            new CategoriaDTO("oficina",    "Oficina"),
            new CategoriaDTO("bebidas",    "Bebidas"),
            new CategoriaDTO("tecnologia", "Tecnología"),
            new CategoriaDTO("bolsos",     "Bolsos")
    );

    @GetMapping
    public List<CategoriaDTO> listar() {
        return CATEGORIAS;
    }
}
