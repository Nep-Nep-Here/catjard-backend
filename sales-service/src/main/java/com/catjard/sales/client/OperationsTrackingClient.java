package com.catjard.sales.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// Marca hitos de tracking en operations-service (POST -> compatible con el
// cliente Feign por defecto).
@FeignClient(name = "operations-service")
public interface OperationsTrackingClient {

    @PostMapping("/api/tracking/pedido/{codigo}")
    void marcarHito(@PathVariable("codigo") String codigo, @RequestBody MarcarHitoBody body);
}
