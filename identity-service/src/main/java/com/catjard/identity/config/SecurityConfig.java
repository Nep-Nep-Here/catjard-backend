package com.catjard.identity.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// MECANISMO DE SEGURIDAD: configuración central de Spring Security para identity-service.
// Aquí se definen: filtro JWT, política de sesiones, rutas públicas, encoder BCrypt y
// la activación de seguridad a nivel de método (@PreAuthorize, @Secured). ss
@Configuration
@EnableMethodSecurity   // habilita autorización por método (@PreAuthorize, hasRole...)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF desactivado: se permite porque la API es REST stateless (no usa cookies de sesión).
                .csrf(csrf -> csrf.disable())
                // Sesiones STATELESS: cada request se autentica solo con el JWT, no se guarda HttpSession.
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos: login y registro no requieren JWT.
                        // Swagger y actuator se exponen para documentación/monitoreo en este proyecto.
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/registro",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/actuator/**"
                        ).permitAll()
                        // El resto de rutas requiere un JWT válido (validado por jwtFilter).
                        .anyRequest().authenticated()
                )
                // Inserta el filtro JWT antes del de usuario/contraseña por defecto.
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // MECANISMO DE SEGURIDAD: BCrypt como algoritmo de hash de contraseñas.
    // BCrypt incluye sal aleatoria por hash y es resistente a fuerza bruta por su costo
    // computacional (work factor por defecto = 10).
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }
}
