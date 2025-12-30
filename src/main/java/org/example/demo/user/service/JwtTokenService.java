package org.example.demo.user.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import org.example.demo.user.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtTokenService {

    private final String secret;
    private final long ttlSeconds;

    public JwtTokenService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.ttl-seconds:3600}") long ttlSeconds
    ) {
        this.secret = secret;
        this.ttlSeconds = ttlSeconds;
    }

    public String generateAccessToken(User user) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("app.jwt.secret must be at least 32 bytes for HS256");
        }

        Instant now = Instant.now();
        Instant exp = now.plusSeconds(ttlSeconds);

        return Jwts.builder()
                .subject(String.valueOf(user.getUserId()))
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claim("displayName", user.getDisplayName())
                .claim("role", user.getRole().name())
                .signWith(Keys.hmacShaKeyFor(keyBytes), Jwts.SIG.HS256)
                .compact();
    }
}
