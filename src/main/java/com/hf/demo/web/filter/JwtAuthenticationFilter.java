package com.hf.demo.web.filter;

import com.hf.demo.service.impl.UserDetailsServiceImpl;
import com.hf.demo.util.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Resource
    private JwtUtils jwtUtils;

    @Resource
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 从请求头提取 Token
        String token = parseJwt(request);

        // 2. 如果有 Token 且有效
        if (token != null && jwtUtils.validateToken(token)) {
            try {
                // 3. 从 Token 里把用户名拿出来
                String username = jwtUtils.extractUsername(token);

                // 4. 加载用户详细信息 (查数据库)
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // 5. 创建认证对象 (这是一个标准的 Spring Security 认证令牌)
                // 参数：用户信息、密码(null)、权限列表
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                // 绑定请求详情 (IP 等)
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 6. 【关键】把这个认证对象塞给 Spring Security 的上下文
                // 这一步之后，Spring Security 就知道“当前用户已登录”
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } catch (Exception e) {
                // 如果 Token 验证过程出错，不要抛异常，直接让它继续走
                // 后面 Spring Security 会发现 SecurityContext 是空的，自然会报 403
                logger.error("无法设置用户认证: {}", e);
            }
        }

        // 7. 继续执行过滤器链 (放行)
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