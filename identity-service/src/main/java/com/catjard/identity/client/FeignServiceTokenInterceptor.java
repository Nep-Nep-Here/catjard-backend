package com.catjard.identity.client;

import com.catjard.identity.service.JwtService;
import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// El registro es un endpoint PÚBLICO: no hay JWT de usuario que propagar.
// Por eso identity adjunta a sus llamadas Feign un token de SERVICIO de vida
// corta (rol gerente), firmado con el secreto HMAC compartido, que crm-service
// valida con su mismo JwtAuthenticationFilter.
@Configuration
@RequiredArgsConstructor
public class FeignServiceTokenInterceptor {

    private final JwtService jwt;

    @Bean
    public RequestInterceptor serviceTokenInterceptor() {
        return template -> template.header("Authorization", "Bearer " + jwt.generateServiceToken());
    }
}
