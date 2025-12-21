package com.hf.demo.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class JwtUtils {

    private static final String CLAIM_TOKEN_TYPE = "token_type";
    private static final String CLAIM_AUTH = "auth";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final JwtProperties props;
    private final Key key;

    public JwtUtils(JwtProperties props) {
        this.props = props;
        byte[] bytes = props.secret().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalArgumentException("jwt.secret too short for HS256 (need >= 32 bytes)");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    public String generateAccessToken(String username, List<String> authCodes) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .claim(CLAIM_TOKEN_TYPE, TYPE_ACCESS)
                .claim(CLAIM_AUTH, authCodes)
                .setExpiration(new Date(System.currentTimeMillis() + props.accessExpirationMs()))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String username, String refreshId) {
        return Jwts.builder()
                .setSubject(username)
                .setId(refreshId) // jti
                .claim(CLAIM_TOKEN_TYPE, TYPE_REFRESH)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + props.refreshExpirationMs()))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("[JWT][INVALID] {}", e.getMessage());
            return false;
        }
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token) {
        return extractUsername(parseClaims(token));
    }

    public String extractRefreshId(String token) {
        return extractRefreshId(parseClaims(token));
    }

    public String extractTokenType(String token) {
        return extractTokenType(parseClaims(token));
    }

    public List<String> extractAuth(String token) {
        return extractAuth(parseClaims(token));
    }

    public String extractUsername(Claims claims) {
        return claims.getSubject();
    }

    public String extractRefreshId(Claims claims) {
        return claims.getId();
    }

    public String extractTokenType(Claims claims) {
        Object v = claims.get("token_type");
        return v == null ? null : v.toString();
    }

    public List<String> extractAuth(Claims claims) {
        Object v = claims.get("auth");
        if (v == null) return List.of();
        if (v instanceof List<?> list) {
            return list.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .toList();
        }
        return List.of(v.toString());
    }
}
