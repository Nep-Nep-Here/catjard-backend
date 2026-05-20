package com.catjard.identity.client;

import com.catjard.identity.dto.ClienteRefDTO;
import com.catjard.identity.dto.CrearClienteCrmDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// Cliente Feign hacia crm-service. Se usa en el auto-registro para crear (o
// reutilizar por RUC) el ClienteCRM y obtener su id, que se guarda en el Usuario.
@FeignClient(name = "crm-service")
public interface CrmClient {

    @PostMapping("/api/clientes/registro")
    ClienteRefDTO registrarOReutilizar(@RequestBody CrearClienteCrmDTO body);
}
