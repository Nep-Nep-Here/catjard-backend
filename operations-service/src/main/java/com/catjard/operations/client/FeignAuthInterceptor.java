package com.catjard.operations.client;

import feign.Client;
import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.cloud.openfeign.loadbalancer.FeignBlockingLoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;

@Configuration
public class FeignAuthInterceptor {

    @Bean
    public RequestInterceptor authForwardInterceptor() {
        return template -> {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return;
            HttpServletRequest req = attrs.getRequest();
            String header = req.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                template.header("Authorization", header);
            }
        };
    }

    // Spring Cloud OpenFeign 5.x ya NO autoconfigura OkHttp (la propiedad
    // spring.cloud.openfeign.okhttp.enabled quedo inerte). Registramos el
    // cliente a mano porque el default (feign.Client$Default → HttpURLConnection)
    // no soporta PATCH y SalesClient usa @PatchMapping.
    // Lo envolvemos en FeignBlockingLoadBalancerClient para que siga resolviendo
    // los hostnames lógicos ("sales-service") vía Eureka; sin esa envoltura
    // OkHttp intenta DNS directo y falla con UnknownHostException.
    @Bean
    public Client feignClient(LoadBalancerClient loadBalancer,
                              LoadBalancerClientFactory loadBalancerClientFactory) {
        return new FeignBlockingLoadBalancerClient(
                new feign.okhttp.OkHttpClient(),
                loadBalancer,
                loadBalancerClientFactory,
                Collections.emptyList());
    }
}
