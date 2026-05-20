package com.catjard.sales.client;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

// Token de servicio (service-to-service): sales lo usa para llamar a operations
// e inventory desde flujos sin JWT de usuario (o con rol que no autoriza).
// Firmado con el MISMO secreto HMAC compartido; rol gerente; vida corta.
@Component
public class ServiceTokenProvider {

    private final SecretKey key;

    public ServiceTokenProvider(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
    }

    public String token() {
        var now = new Date();
        var exp = new Date(now.getTime() + 60_000); // 60 s
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "gerente");
        claims.put("name", "sales-service");
        return Jwts.builder()
                .subject("svc-sales@catjard")
                .claims(claims)
                .issuedAt(now)
                .expiration(exp)
                .signWith(key)
                .compact();
    }
}
