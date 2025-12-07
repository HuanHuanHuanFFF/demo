package com.hf.demo.util;

import java.util.concurrent.ThreadLocalRandom;

public final class RandomUtils {

    private RandomUtils() {
    }

    /**
     * 生成 [0, maxSeconds] 之间的随机秒数
     */
    public static long randomJitterSeconds(long maxSeconds) {
        if (maxSeconds <= 0) {
            return 0;
        }
        // [0, maxSeconds]，注意要 +1
        return ThreadLocalRandom.current().nextLong(0, maxSeconds + 1);
    }
}
