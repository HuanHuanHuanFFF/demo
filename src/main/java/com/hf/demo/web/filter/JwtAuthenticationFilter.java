package com.hf.demo.web.filter;

import com.hf.demo.util.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Resource
    private JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 从请求头提取 Token
        String token = parseJwt(request);

        // 没带 token：直接放行（后面会因为没认证而 401）
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 先验签+过期（无效 token 当作没登录）
        if (!jwtUtils.validateToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 只允许 access token 走认证
        String tokenType = jwtUtils.extractTokenType(token);
        if (!"access".equals(tokenType)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String username = jwtUtils.extractUsername(token);
            List<String> authCodes = jwtUtils.extractAuth(token);

            var authorities = authCodes.stream()
                    .filter(s -> s != null && !s.isBlank())
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .distinct()
                    .map(SimpleGrantedAuthority::new)
                    .toList();
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authToken);

        } catch (Exception e) {
            logger.error("无法设置用户认证: {}", e);
        }
        filterChain.doFilter(request, response);
    }

    /**
     * 辅助方法：从 Authorization 头里解析出纯 Token
     * 格式通常是: "Bearer eyJhbGci..."
     */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7); // 去掉 "Bearer " 前缀
        }
        return null;
    }
}