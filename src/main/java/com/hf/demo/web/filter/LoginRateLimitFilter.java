package com.hf.demo.web.filter;

import com.hf.demo.util.IpUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

@Slf4j
@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final int MAX_REQ_PER_MIN = 5;
    private static final long WINDOW_TTL_SECONDS = 60L;
    private static final String KEY_PREFIX = "rl:login:ip:";

    private static final DefaultRedisScript<Long> INCR_EXPIRE_SCRIPT = new DefaultRedisScript<>(
            """
                    local c = redis.call('INCR',KEYS[1])
                    if c == 1 then
                        redis.call('EXPIRE',KEYS[1],ARGV[1])
                    end
                    return c
                    """,
            Long.class
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !("POST".equalsIgnoreCase(request.getMethod()) && "/auth/login".equals(request.getRequestURI()));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String ip = IpUtils.getClientIp(request);
        String minute = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        String k = KEY_PREFIX + ip + ":" + minute;
        Long cnt;
        try {
            cnt = stringRedisTemplate.execute(
                    INCR_EXPIRE_SCRIPT,
                    Collections.singletonList(k),
                    String.valueOf(WINDOW_TTL_SECONDS)
            );
        } catch (Exception e) {
            log.error("[RATE_LIMIT][DEGRADED] redis unavailable, allow login. err={}", e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }
        if (cnt != null && cnt > MAX_REQ_PER_MIN) {
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":429,\"msg\":\"请求过于频繁，请稍后再试\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
