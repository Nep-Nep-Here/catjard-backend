package com.catjard.solicitudes.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.http.HttpMethod.POST;

// MECANISMO DE SEGURIDAD: Spring Security stateless con filtro JWT.
// Toda la API de solicitudes exige autenticacion; la autorizacion fina por rol
// se aplica con @PreAuthorize en el controlador.
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Envio publico de solicitudes desde la web (sin login), como el form de leads.
                        .requestMatchers(POST, "/api/solicitudes").permitAll()
                        // Registro de respaldos del cron del Droplet: no hay usuario/JWT;
                        // el controlador valida el token compartido X-Backup-Token.
                        .requestMatchers(POST, "/api/continuidad/respaldos/script").permitAll()
                        .requestMatchers(
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/actuator/**",
                                // El dispatch interno a /error no re-autentica en la cadena
                                // stateless: sin esto, toda excepcion sale como 403 vacio.
                                "/error"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
