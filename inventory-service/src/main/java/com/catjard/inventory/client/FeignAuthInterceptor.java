package com.catjard.inventory.client;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Configuration
public class FeignAuthInterceptor {

    @Bean
    public RequestInterceptor authForwardInterceptor() {
        return template -> {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                log.warn("[Feign->{}] RequestContextHolder es null, no se puede propagar Authorization. URL={}",
                        template.feignTarget().name(), template.url());
                return;
            }
            HttpServletRequest req = attrs.getRequest();
            String header = req.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                template.header("Authorization", header);
                log.debug("[Feign->{}] Authorization propagado. URL={}",
                        template.feignTarget().name(), template.url());
            } else {
                log.warn("[Feign->{}] No hay Authorization header en el request entrante. URL={}",
                        template.feignTarget().name(), template.url());
            }
        };
    }
}
