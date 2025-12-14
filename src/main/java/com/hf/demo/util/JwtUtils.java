package com.hf.demo.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.nio.charset.StandardCharsets;

@Component
public class JwtUtils {

    // 从配置文件读取 secret
    @Value("${jwt.secret}")
    private String secret;

    // 从配置文件读取过期时间
    @Value("${jwt.expiration}")
    private long expiration;

    private Key key;

    // @PostConstruct 保证在 Bean 初始化完成后执行，此时 @Value 已经注入了值
    @PostConstruct
    public void init() {
        // 使用配置的字符串生成固定的 Key
        // HMAC-SHA 算法要求密钥必须是 bytes
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 Token
     */
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析 Token (验证并获取用户名)
     */
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * 验证 Token 是否有效
     */
    public boolean validateToken(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (Exception e) {
            // 这里可以细化异常处理，比如过期异常(ExpiredJwtException)还是签名错误(SignatureException)
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
}