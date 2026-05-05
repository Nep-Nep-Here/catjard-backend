package com.catjard.operations.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "sales-service")
public interface SalesClient {

    @PatchMapping("/api/pedidos/codigo/{codigo}")
    void actualizarPedidoPorCodigo(@PathVariable("codigo") String codigo, @RequestBody Map<String, Object> body);
}
