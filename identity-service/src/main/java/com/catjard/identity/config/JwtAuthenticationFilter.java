package com.catjard.identity.config;

import com.catjard.identity.service.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// MECANISMO DE SEGURIDAD: filtro de autenticación JWT por request.
// Se ejecuta una vez por cada petición HTTP (OncePerRequestFilter). Si el header
// Authorization trae un Bearer token válido, monta el Authentication en el
// SecurityContext con el rol como ROLE_<rol>, lo que habilita @PreAuthorize y
// authorizeHttpRequests en SecurityConfig.
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwt;

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7); // quita el prefijo "Bearer "
            try {
                // Verifica firma y vencimiento del JWT (lanza JwtException si falla).
                var claims = jwt.parse(token);
                String email = claims.getSubject();
                String role = claims.get("role", String.class);
                // Mapeo del claim "role" al GrantedAuthority "ROLE_xxx" que usa Spring Security.
                var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
                var authentication = new UsernamePasswordAuthenticationToken(email, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException ignored) {
                // Token inválido/expirado: la request sigue sin autenticación y
                // SecurityConfig la rechazará con 401/403 según corresponda.
            }
        }
        chain.doFilter(req, res);
    }
}
