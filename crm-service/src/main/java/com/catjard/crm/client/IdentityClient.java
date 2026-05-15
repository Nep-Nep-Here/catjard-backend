package com.catjard.crm.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "identity-service")
public interface IdentityClient {

    @PostMapping("/api/usuarios/cliente-from-lead")
    Map<String, Object> crearClienteDesdeLead(@RequestBody Map<String, Object> body);
}
