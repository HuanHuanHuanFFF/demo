package com.hf.demo.util;

import jakarta.servlet.http.HttpServletRequest;

public final class IpUtils {
    private IpUtils() {
    }

    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        if (ip == null || ip.isBlank()) return "unknown";

        // 兼容 IPv6 loopback
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) return "127.0.0.1";

        // 兼容带 zone id 的 IPv6（如 fe80::1%lo0）
        int zoneIdx = ip.indexOf('%');
        if (zoneIdx > 0) ip = ip.substring(0, zoneIdx);

        return ip;
    }
}
