package com.catjard.identity.service;

import com.catjard.identity.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

// MECANISMO DE SEGURIDAD: JWT (JSON Web Token)
// Servicio responsable de emitir y validar los tokens que autentican a los usuarios
// en todos los microservicios. Firma con HMAC-SHA usando la clave configurada en
// jwt.secret (application.properties).
@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration-ms}") long expirationMs) {
        // Clave simétrica HMAC reconstruida desde el secret base64.
        this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
        this.expirationMs = expirationMs;
    }

    // Emite un JWT firmado con los claims del usuario (id, rol, nombre, clienteId si aplica) y vencimiento.
    public String generate(Usuario u) {
        var now = new Date();
        var exp = new Date(now.getTime() + expirationMs);
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", u.getId());
        claims.put("role", u.getRol().name());
        claims.put("name", u.getNombre());
        if (u.getClienteId() != null) claims.put("clienteId", u.getClienteId());
        return Jwts.builder()
                .subject(u.getEmail())
                .claims(claims)
                .issuedAt(now)
                .expiration(exp)
                .signWith(key) // firma HMAC para evitar manipulación del token
                .compact();
    }

    // Verifica firma y vencimiento. Si el token fue alterado o expiró, lanza JwtException.
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getExpirationMs() {
        return expirationMs;
    }
}
