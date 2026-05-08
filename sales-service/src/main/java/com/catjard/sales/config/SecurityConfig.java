package com.catjard.sales.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// MECANISMO DE SEGURIDAD: configuración de Spring Security para sales-service.
// Aplica el mismo modelo que identity-service (defensa en profundidad: cada
// microservicio valida el JWT por su cuenta, no confía solo en el gateway).
// La diferencia es que aquí NO hay endpoints públicos de auth — todo lo de negocio
// requiere JWT válido, salvo Swagger/Actuator.
@Configuration
@EnableMethodSecurity   // habilita @PreAuthorize en los services/controllers
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF desactivado: API REST stateless con JWT (sin cookies de sesión).
                .csrf(csrf -> csrf.disable())
                // Sin estado de sesión: cada request se autentica solo con su JWT.
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/actuator/**"
                        ).permitAll()
                        // Cualquier endpoint de cotizaciones/pedidos requiere JWT.
                        .anyRequest().authenticated()
                )
                // Filtro JWT antes del filtro estándar de usuario/contraseña.
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
