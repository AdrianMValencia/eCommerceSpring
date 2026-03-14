package com.amechato.ecommerce_api.backend.infrastructure.security;

import com.amechato.ecommerce_api.backend.domain.models.User;
import com.amechato.ecommerce_api.backend.infrastructure.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtProperties _jwtProperties;

    public JwtService(JwtProperties jwtProperties) {
        _jwtProperties = jwtProperties;
    }

    public String generateToken(User user) {
        // Paso JWT-1: definir la fecha de emision y expiracion del token.
        Date now = new Date();
        Date expiration = new Date(now.getTime() + _jwtProperties.getExpirationMs());

        // Paso JWT-2: construir el token con claims de identidad y rol usados por la autorizacion.
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("role", user.getUserType().name())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    public Claims extractClaims(String token) {
        // Paso JWT-3: parsear el token y validar la firma.
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token) {
        try {
            // Paso JWT-4: el token es valido solo si la firma es correcta y no esta expirado.
            Claims claims = extractClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    private SecretKey getSigningKey() {
        // Paso JWT-5: exigir una longitud minima segura para firmar con HMAC.
        String secret = _jwtProperties.getSecret();
        if (secret == null || secret.isBlank() || secret.length() < 32) {
            throw new IllegalStateException("JWT secret must have at least 32 characters");
        }

        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
