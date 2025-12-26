package com.hf.demo.infra.redis;

import com.hf.demo.util.HashUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedisSessionStore {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final String AUTH_SESS_PREFIX = "auth:sess:";
    private static final String F_LOGIN_AT = "loginAt";
    private static final String F_LAST_SEEN_AT = "lastSeenAt";
    private static final String F_UA = "ua";

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshTtlMs;

    public void upsertOnLogin(String username, String ua, long nowMs) {
        String k = AUTH_SESS_PREFIX + username + ":" + HashUtils.shortHash(ua);
        var h = stringRedisTemplate.opsForHash();
        h.put(k, F_LOGIN_AT, String.valueOf(nowMs));
        h.put(k, F_LAST_SEEN_AT, String.valueOf(nowMs));
        h.put(k, F_UA, ua == null ? "" : ua);
        stringRedisTemplate.expire(k, Duration.ofMillis(refreshTtlMs));
    }

    public void touchOnRefresh(String username, String ua, long nowMs) {
        String k = AUTH_SESS_PREFIX + username + ":" + HashUtils.shortHash(ua);
        var h = stringRedisTemplate.opsForHash();
        h.put(k, F_LAST_SEEN_AT, String.valueOf(nowMs));
        stringRedisTemplate.expire(k, Duration.ofMillis(refreshTtlMs));
    }

    public void deleteOnLogout(String username, String ua) {
        String k = AUTH_SESS_PREFIX + username + ":" + HashUtils.shortHash(ua);
        stringRedisTemplate.delete(k);
    }
}
