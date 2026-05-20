package com.catjard.sales.client;

import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Adjunta el token de servicio a todas las llamadas Feign de sales-service.
@Configuration
@RequiredArgsConstructor
public class FeignServiceTokenInterceptor {

    private final ServiceTokenProvider tokens;

    @Bean
    public RequestInterceptor salesServiceTokenInterceptor() {
        return template -> template.header("Authorization", "Bearer " + tokens.token());
    }
}
