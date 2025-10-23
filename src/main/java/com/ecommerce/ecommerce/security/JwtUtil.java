package com.ecommerce.ecommerce.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Simple JWT helper. In production move secret to env variable.
 */
@Component
public class JwtUtil {

    // Use a long secret, move to env/config in production
    private static final String SECRET = "replace_this_with_very_long_and_secure_secret_key_for_prod_2025";
    private static final long EXP_MILLIS = 1000L * 60 * 60 * 24 * 7; // 7 days

    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    // generate JWT with userId and email as claims
    public String generateToken(Long userId, String email) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("email", email)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + EXP_MILLIS))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // validate token and return claims
    public Jws<Claims> parseClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }

    public boolean validate(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public Long getUserId(String token) {
        Claims claims = parseClaims(token).getBody();
        return Long.parseLong(claims.getSubject());
    }

    public LocalDateTime getExpirationDateTime(String token) {
        Claims claims = parseClaims(token).getBody();
        Date exp = claims.getExpiration();
        return LocalDateTime.ofInstant(exp.toInstant(), ZoneId.of("Africa/Dar_es_Salaam"));
    }
}