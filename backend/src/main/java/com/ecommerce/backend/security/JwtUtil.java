package com.ecommerce.backend.security;

import java.security.Key;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class JwtUtil {

    private static final int MIN_SECRET_LENGTH = 32;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms:3600000}")
    private long expirationTimeMs;

    private Key signingKey;

    @PostConstruct
    public void init() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT secret is not configured");
        }

        if (secret.length() < MIN_SECRET_LENGTH) {
            throw new IllegalStateException("JWT secret must be at least 32 characters");
        }

        signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    
    private Key getSigningKey() {
        return signingKey;
    }

    // Generate JWT token
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .claim("role", role)
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTimeMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Extract email from token
        public String extractEmail(String token) {
        
        return getClaims(token).getSubject();
    }

    // Extract role from token
    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    //Validate token
    public boolean validateToken(String token, String email) {
        return extractEmail(token).equals(email) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
