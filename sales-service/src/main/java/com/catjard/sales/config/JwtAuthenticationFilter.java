package com.catjard.sales.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

// MECANISMO DE SEGURIDAD: validación de JWT en sales-service.
// Cada microservicio aguas abajo del gateway revalida el JWT con la MISMA clave HMAC
// que identity-service (compartida vía jwt.secret). Esto evita que un atacante que
// llegue al servicio sin pasar por el gateway pueda saltarse la autenticación.
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final SecretKey key;

    public JwtAuthenticationFilter(@Value("${jwt.secret}") String secret) {
        // Misma clave HMAC que identity-service → permite verificar tokens emitidos allá.
        this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                // Verifica firma y vencimiento del JWT en cada request.
                Claims claims = Jwts.parser()
                        .verifyWith(key)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
                String email = claims.getSubject();
                String role = claims.get("role", String.class);
                String name = claims.get("name", String.class);
                // Convierte el rol del JWT en authority Spring Security (ROLE_xxx).
                var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
                // Guardamos el nombre del usuario en `credentials` (que normalmente
                // va null en JWT) para que los services lo lean sin re-parsear el token.
                var auth = new UsernamePasswordAuthenticationToken(email, name, authorities);
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (JwtException ignored) {
                // Token inválido/expirado: queda no autenticado y SecurityConfig responde 401/403.
            }
        }
        chain.doFilter(req, res);
    }
}
