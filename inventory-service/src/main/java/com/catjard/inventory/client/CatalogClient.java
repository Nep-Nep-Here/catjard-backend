package com.catjard.inventory.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "catalog-service")
public interface CatalogClient {

    @PatchMapping("/api/productos/{id}/stock")
    void actualizarStock(@PathVariable("id") Long productoId, @RequestBody Map<String, Integer> body);
}
