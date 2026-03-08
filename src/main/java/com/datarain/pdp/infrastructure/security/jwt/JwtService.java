package com.datarain.pdp.infrastructure.security.jwt;

import com.datarain.pdp.infrastructure.security.Role;
import com.datarain.pdp.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Service
public class JwtService {

    // اصلاح شد: SECRET_KEY از hardcode در آمد و از application.properties خونده میشه
    // در production باید از ConfigMap/Secret خوندن
    private final SecretKey key;
    private final long accessTokenExpirationMs;

    public JwtService(
            @Value("${pdp.jwt.secret:super-secret-key-that-must-be-at-least-32-chars-long}") String secret,
            @Value("${pdp.jwt.access-token-expiration-ms:900000}") long accessTokenExpirationMs
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpirationMs = accessTokenExpirationMs;
    }

    // اصلاح شد: userId هم به claims اضافه شد تا تو filter نیازی به DB query نباشه
    public String generateAccessToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("userId", user.getId().toString())
                .claim("roles", user.getRoles()
                        .stream()
                        .map(Role::name)
                        .toList())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
