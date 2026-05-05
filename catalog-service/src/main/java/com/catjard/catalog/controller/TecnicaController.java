package com.catjard.catalog.controller;

import com.catjard.catalog.service.ProductoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Tecnicas", description = "Tecnicas de personalizacion disponibles")
@RestController
@RequestMapping("/api/tecnicas")
public class TecnicaController {

    @GetMapping
    public List<String> listar() {
        return ProductoService.TECNICAS_VALIDAS;
    }
}
