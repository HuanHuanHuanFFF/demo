package com.hf.demo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthRefreshConcurrencyTest {

    @Autowired
    private TestRestTemplate rest;

    private final ObjectMapper om = new ObjectMapper();

    @Test
    void refreshToken_should_only_be_usable_once_concurrently() throws Exception {
        // 1) 先登录拿 refreshToken（用你自己的测试账号）
        String refreshToken = loginAndGetRefreshToken("test", "123");
        assertNotNull(refreshToken);

        // 2) 并发打 refresh（同一个 refreshToken）
        int threads = 30;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        AtomicInteger ok = new AtomicInteger(0);
        AtomicInteger fail = new AtomicInteger(0);

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();

                    ResponseEntity<String> r = callRefresh(refreshToken);

                    // 你的 Result 里如果 SUCCESS code=0（你刚规范过），就用这个判断
                    // 如果你 SUCCESS 用的不是 0，把这里改成你的成功码
                    int code = extractCode(r.getBody());
                    if (code == 0) ok.incrementAndGet();
                    else fail.incrementAndGet();

                } catch (Exception e) {
                    fail.incrementAndGet();
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await(5, TimeUnit.SECONDS);
        start.countDown();
        done.await(10, TimeUnit.SECONDS);
        pool.shutdownNow();

        // 3) 断言：只能成功 1 次
        assertEquals(1, ok.get(), "同一个 refreshToken 并发刷新只能成功一次");
        assertEquals(threads - 1, fail.get(), "其余请求必须失败");
        System.out.println("threads=" + threads + ", ok=" + ok.get() + ", fail=" + fail.get());
    }

    private ResponseEntity<String> callRefresh(String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 你现在 refresh 接口用 body 传 refreshToken
        Map<String, Object> body = Map.of("refreshToken", refreshToken);
        HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);

        return rest.postForEntity("/auth/refresh", req, String.class);
    }

    private String loginAndGetRefreshToken(String username, String password) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of("username", username, "password", password);
        HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);

        ResponseEntity<String> resp = rest.postForEntity("/auth/login", req, String.class);
        assertNotNull(resp.getBody());

        JsonNode root = om.readTree(resp.getBody());
        // Result 的结构：{ code, msg, data: { accessToken, refreshToken } }
        return root.path("data").path("refreshToken").asText(null);
    }

    private int extractCode(String body) throws Exception {
        if (body == null) return -1;
        JsonNode root = om.readTree(body);
        return root.path("code").asInt(-1);
    }
}
