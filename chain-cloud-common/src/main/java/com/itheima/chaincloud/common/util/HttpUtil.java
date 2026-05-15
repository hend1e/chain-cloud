package com.itheima.chaincloud.common.util;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
public class HttpUtil {

    /**
     * 获取请求的 HttpServletRequest 对象
     */
    public static HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            return attributes.getRequest();
        }
        return null;
    }

    /**
     * 获取客户端 IP 地址
     *
     * @return IP 地址
     */
    public static String getClientIp() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return "unknown";
        }

        // 1. 获取 X-Forwarded-For（负载均衡、代理服务器）
        String ip = request.getHeader("X-Forwarded-For");
        if (StrUtil.isNotEmpty(ip) && !"unknown".equalsIgnoreCase(ip)) {
            // 多个 IP 时取第一个
            int index = ip.indexOf(",");
            if (index != -1) {
                ip = ip.substring(0, index);
            }
            return ip;
        }

        // 2. 获取 X-Real-IP（Nginx 代理）
        ip = request.getHeader("X-Real-IP");
        if (StrUtil.isNotEmpty(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        // 3. 获取 Proxy-Client-IP（WebLogic）
        ip = request.getHeader("Proxy-Client-IP");
        if (StrUtil.isNotEmpty(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        // 4. 获取 WL-Proxy-Client-IP（WebLogic）
        ip = request.getHeader("WL-Proxy-Client-IP");
        if (StrUtil.isNotEmpty(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        // 5. 获取 HTTP_CLIENT_IP（某些代理服务器）
        ip = request.getHeader("HTTP_CLIENT_IP");
        if (StrUtil.isNotEmpty(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        // 6. 获取 HTTP_X_FORWARDED_FOR（Apache 代理）
        ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (StrUtil.isNotEmpty(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        // 7. 最后使用 getRemoteAddr()
        ip = request.getRemoteAddr();

        // 如果是本地回环地址，尝试获取本机真实 IP
        if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
            try {
                InetAddress inetAddress = InetAddress.getLocalHost();
                ip = inetAddress.getHostAddress();
            } catch (UnknownHostException e) {
                log.error("获取本机 IP 失败", e);
            }
        }

        return ip;
    }

    /**
     * 获取设备信息（User-Agent）
     *
     * @return 设备信息
     */
    public static String getDeviceInfo() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return "unknown";
        }

        String userAgent = request.getHeader("User-Agent");
        if (StrUtil.isEmpty(userAgent)) {
            return "unknown";
        }

        // 解析 User-Agent 信息
        StringBuilder deviceInfo = new StringBuilder();

        // ========== 判断操作系统 ==========
        String osType = detectOperatingSystem(userAgent);
        deviceInfo.append(osType);

        // ========== 判断特殊应用 ==========
        String appType = detectApplication(userAgent);
        if (StrUtil.isNotEmpty(appType)) {
            deviceInfo.append(" | ").append(appType);
        } else {
            // 如果没有特殊应用，则判断浏览器
            String browser = detectBrowser(userAgent);
            deviceInfo.append(" | ").append(browser);
        }

        // ========== 判断移动设备类型 ==========
        String deviceType = detectDeviceType(userAgent);
        if (StrUtil.isNotEmpty(deviceType)) {
            deviceInfo.append(" | ").append(deviceType);
        }

        return deviceInfo.toString();
    }

    /**
     * 检测操作系统
     */
    private static String detectOperatingSystem(String userAgent) {
        // Windows 系统
        if (userAgent.contains("Windows NT 10")) {
            return "Windows 10";
        } else if (userAgent.contains("Windows NT 6.3")) {
            return "Windows 8.1";
        } else if (userAgent.contains("Windows NT 6.2")) {
            return "Windows 8";
        } else if (userAgent.contains("Windows NT 6.1")) {
            return "Windows 7";
        } else if (userAgent.contains("Windows NT 6.0")) {
            return "Windows Vista";
        }

        // Mac OS
        if (userAgent.contains("Mac OS X")) {
            return "Mac OS";
        }

        // Android
        if (userAgent.contains("Android")) {
            // 提取 Android 版本号
            int index = userAgent.indexOf("Android");
            if (index != -1 && index + 8 < userAgent.length()) {
                String version = userAgent.substring(index + 8, Math.min(index + 11, userAgent.length()));
                return "Android " + version;
            }
            return "Android";
        }

        // iOS 设备
        if (userAgent.contains("iPhone")) {
            return "iPhone";
        } else if (userAgent.contains("iPad")) {
            return "iPad";
        } else if (userAgent.contains("iPod")) {
            return "iPod";
        }

        // 其他系统
        if (userAgent.contains("Linux")) {
            return "Linux";
        }
        if (userAgent.contains("CrOS")) {
            return "Chrome OS";
        }

        return "Unknown OS";
    }

    /**
     * 检测特殊应用（微信、QQ、微博等）
     */
    private static String detectApplication(String userAgent) {
        // 微信
        if (userAgent.contains("MicroMessenger") || userAgent.contains("WeChat")) {
            // 尝试提取微信版本号
            int index = userAgent.indexOf("MicroMessenger");
            if (index != -1 && index + 15 < userAgent.length()) {
                String version = userAgent.substring(index + 15, Math.min(index + 20, userAgent.length()));
                return "WeChat " + version.trim();
            }
            return "WeChat";
        }

        // QQ
        if (userAgent.contains("QQ/")) {
            return "QQ";
        }

        // QQ 浏览器
        if (userAgent.contains("MQQBrowser")) {
            return "QQ Browser";
        }

        // 微博
        if (userAgent.contains("Weibo")) {
            return "Weibo";
        }

        // 钉钉
        if (userAgent.contains("DingTalk")) {
            return "DingTalk";
        }

        // 企业微信
        if (userAgent.contains("WXWork")) {
            return "WXWork";
        }

        return null;
    }

    /**
     * 检测浏览器类型
     */
    private static String detectBrowser(String userAgent) {
        // Edge (基于 Chromium)
        if (userAgent.contains("Edg")) {
            return "Edge";
        }

        // 360 浏览器
        if (userAgent.contains("360SE") || userAgent.contains("360EE")) {
            return "360 Browser";
        }

        // UC 浏览器
        if (userAgent.contains("UCBrowser")) {
            return "UC Browser";
        }

        // Chrome
        if (userAgent.contains("Chrome")) {
            return "Chrome";
        }

        // Firefox
        if (userAgent.contains("Firefox")) {
            return "Firefox";
        }

        // Safari (注意：Chrome 也包含 Safari，所以要放在后面)
        if (userAgent.contains("Safari")) {
            return "Safari";
        }

        // IE
        if (userAgent.contains("MSIE") || userAgent.contains("Trident")) {
            return "IE";
        }

        return "Unknown Browser";
    }

    /**
     * 检测设备类型（手机/平板）
     */
    private static String detectDeviceType(String userAgent) {
        // iPad 优先判断（因为 iPad 的 User-Agent 也可能包含 Mobile）
        if (userAgent.contains("iPad")) {
            return "Tablet";
        }

        // 移动设备
        if (userAgent.contains("Mobile") || userAgent.contains("Android") && !userAgent.contains("TV")) {
            return "Mobile";
        }

        // 安卓平板
        if (userAgent.contains("Android") && userAgent.contains("Tablet")) {
            return "Tablet";
        }

        return null;
    }
}
