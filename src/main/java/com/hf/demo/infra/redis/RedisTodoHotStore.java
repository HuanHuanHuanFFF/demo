package com.hf.demo.infra.redis;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class RedisTodoHotStore {
    @Resource
    StringRedisTemplate stringRedisTemplate;
    private static final String KEY_PREFIX = "todo:hot:day:";
    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final Duration RETAIN_TTL = Duration.ofDays(30);  // 日榜保留30天
    private static final ZoneId ZONE = ZoneId.systemDefault();

    private String dayKey(LocalDate day) {
        return KEY_PREFIX + day.format(DAY_FMT);
    }

    public void hit(Long todoId) {
        LocalDate today = LocalDate.now(ZONE);
        String key = dayKey(today);
        stringRedisTemplate.opsForZSet().incrementScore(key, String.valueOf(todoId), 1.0);
        stringRedisTemplate.expire(key, RETAIN_TTL);
    }

    public List<HotTodo> top(int n) {
        LocalDate today = LocalDate.now(ZONE);
        String key = dayKey(today);
        Set<ZSetOperations.TypedTuple<String>> set =
                stringRedisTemplate.opsForZSet().reverseRangeWithScores(key, 0, n - 1);
        if (set == null) return List.of();
        List<HotTodo> list = new ArrayList<>(set.size());
        for (var x : set) {
            if (x.getValue() == null || x.getScore() == null) continue;
            list.add(new HotTodo(Long.parseLong(x.getValue()), x.getScore().longValue()));
        }
        return list;
    }

    public record HotTodo(Long todoId, long score) {
    }
}
