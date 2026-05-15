package com.itheima.ms.web.customer.util;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 请求上下文工具类
 * 用于获取用户请求的相关信息
 */
public class RequestContextUtil {

    private static final ThreadLocal<HttpServletRequest> requestHolder = new ThreadLocal<>();

    /**
     * 设置当前请求
     */
    public static void setRequest(HttpServletRequest request) {
        requestHolder.set(request);
    }

    /**
     * 获取当前请求
     */
    public static HttpServletRequest getRequest() {
        return requestHolder.get();
    }

    /**
     * 清除当前请求
     */
    public static void clear() {
        requestHolder.remove();
    }

    /**
     * 获取客户端IP地址
     */
    public static String getClientIp() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return "unknown";
        }

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 获取用户代理信息
     */
    public static String getUserAgent() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return "unknown";
        }
        return request.getHeader("User-Agent");
    }

    /**
     * 获取设备信息
     */
    public static String getDeviceInfo() {
        String userAgent = getUserAgent();
        if (userAgent == null) {
            return "unknown";
        }

        // 简单的设备类型检测
        if (userAgent.toLowerCase().contains("mobile")) {
            return "mobile";
        } else if (userAgent.toLowerCase().contains("tablet")) {
            return "tablet";
        } else {
            return "desktop";
        }
    }

    /**
     * 获取地理位置信息（基于IP）
     */
    public static String getLocation() {
        String ip = getClientIp();
        // 这里可以集成IP地理位置服务
        // 暂时返回IP作为位置信息
        return ip;
    }

    /**
     * 获取完整的风险检测上下文
     */
    public static Map<String, Object> getRiskContext() {
        Map<String, Object> context = new HashMap<>();
        context.put("ip", getClientIp());
        context.put("deviceInfo", getDeviceInfo());
        context.put("location", getLocation());
        context.put("userAgent", getUserAgent());
        context.put("timestamp", System.currentTimeMillis());
        return context;
    }
}