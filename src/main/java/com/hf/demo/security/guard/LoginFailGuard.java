package com.hf.demo.security.guard;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Slf4j
@Component
public class LoginFailGuard {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final String FAIL_KEY_PREFIX = "login:fail:";
    private static final String LOCK_KEY_PREFIX = "login:lock:";
    private static final int MAX_FAILS = 10;
    private static final long FAIL_TTL_SEC = 10 * 60L;
    private static final long LOCK_TTL_SEC = 10 * 60L;

    private static final DefaultRedisScript<Long> FAIL_AND_LOCK_SCRIPT =
            new DefaultRedisScript<>(
                    """
                            if redis.call('EXISTS', KEYS[2]) == 1 then
                              return -1
                            end
                            
                            local c = redis.call('INCR', KEYS[1])
                            if c == 1 then
                              redis.call('EXPIRE', KEYS[1], ARGV[1])
                            end
                            
                            if c >= tonumber(ARGV[2]) then
                              redis.call('SET', KEYS[2], '1', 'EX', ARGV[3])
                              redis.call('DEL', KEYS[1])
                              return -1
                            end
                            
                            return c
                            """,
                    Long.class
            );

    public boolean isLocked(String username) {
        String u = normalize(username);
        String lockKey = LOCK_KEY_PREFIX + u;
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(lockKey));
    }

    /**
     * @param username 登录失败用户名
     * @return -1: 账号已锁定   >=1: 失败次数
     */
    public Long onFail(String username) {
        String u = normalize(username);
        String failKey = FAIL_KEY_PREFIX + u;
        String lockKey = LOCK_KEY_PREFIX + u;
        return stringRedisTemplate.execute(
                FAIL_AND_LOCK_SCRIPT,
                List.of(failKey, lockKey),
                String.valueOf(FAIL_TTL_SEC), String.valueOf(MAX_FAILS), String.valueOf(LOCK_TTL_SEC)
        );
    }

    public void onSuccess(String username) {
        String u = normalize(username);
        stringRedisTemplate.delete(FAIL_KEY_PREFIX + u);
        stringRedisTemplate.delete(LOCK_KEY_PREFIX + u);
    }

    private String normalize(String username) {
        if (username == null) return "null";
        String s = username.trim().toLowerCase(Locale.ROOT);
        return s.isBlank() ? "empty" : s;
    }
}
