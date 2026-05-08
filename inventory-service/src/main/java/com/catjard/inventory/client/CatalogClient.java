package com.catjard.inventory.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "catalog-service")
public interface CatalogClient {

    // POST en lugar de PATCH para evitar el bug de java.net.HttpURLConnection que
    // no soporta PATCH. El endpoint en catalog-service acepta ambos metodos.
    @PostMapping("/api/productos/{id}/stock")
    void actualizarStock(@PathVariable("id") Long productoId, @RequestBody Map<String, Integer> body);
}
