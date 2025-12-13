package com.hf.demo.service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TodoCacheAsyncService {

    private static final String TODO_CACHE_KEY_PREFIX = "todo:byId:";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Async("commonExecutor")
    public void deleteTodoCacheByIdWithDelay(Long id) {
        String k = TODO_CACHE_KEY_PREFIX + id;
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        String name = Thread.currentThread().getName();
        log.info("当前执行双删的线程是: {}", name);
        stringRedisTemplate.delete(k);
        log.info("delay double delete todo cache: {}", id);
    }
}
