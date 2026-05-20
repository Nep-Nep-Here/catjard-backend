package com.catjard.sales.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// Registra movimientos de inventario (POST). inventory-service ajusta el stock
// en catalog-service automáticamente cuando el tipo es "salida".
@FeignClient(name = "inventory-service")
public interface InventoryClient {

    @PostMapping("/api/movimientos")
    void registrarMovimiento(@RequestBody CrearMovimientoBody body);
}
