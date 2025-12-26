package com.hf.demo.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class HashUtils {
    private HashUtils() {
    }

    public static String shortHash(String x) {
        if (x == null) x = "";
        try {
            byte[] d = MessageDigest.getInstance("SHA-256")
                    .digest(x.getBytes(StandardCharsets.UTF_8));
            // 取前 8 字节 => 16 位 hex（够当 key 后缀用了）
            StringBuilder sb = new StringBuilder(16);
            for (int i = 0; i < 8; i++) {
                sb.append(String.format("%02x", d[i]));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
