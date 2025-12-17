package com.hf.demo.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    // accessToken 过期时间（毫秒）
    @Value("${jwt.access-expiration-ms}")
    private long accessExpirationMs;

    // refreshToken 过期时间（毫秒）
    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // ====== accessToken ======
    public String generateAccessToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .claim("token_type", "access")
                .setExpiration(new Date(System.currentTimeMillis() + accessExpirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // ====== refreshToken（带 refreshId -> jti） ======
    public String generateRefreshToken(String username, String refreshId) {
        return Jwts.builder()
                .setSubject(username)
                .setId(refreshId) // jti = refreshId（用于Redis可撤销）
                .claim("token_type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    // 从 refreshToken 解析 refreshId（jti）
    public String extractRefreshId(String token) {
        return extractClaims(token).getId();
    }

    public boolean validateToken(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractTokenType(String token) {
        Object v = extractClaims(token).get("token_type");
        return v == null ? null : v.toString();
    }
}
