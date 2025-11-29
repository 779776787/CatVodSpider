package com.github.catvod.spider.binrunner.bridge;

import com.github.catvod.net.OkHttp;
import com.github.catvod.spider.binrunner.util.BinLogger;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP 请求桥接类
 * 为脚本提供 HTTP 请求功能
 */
public class BinConnect {

    private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    private static final Gson gson = new Gson();

    /**
     * GET 请求
     * @param url 请求 URL
     * @return 响应内容
     */
    public static String get(String url) {
        return get(url, null);
    }

    /**
     * GET 请求（带请求头）
     * @param url 请求 URL
     * @param headers 请求头
     * @return 响应内容
     */
    public static String get(String url, Map<String, String> headers) {
        try {
            Map<String, String> finalHeaders = prepareHeaders(headers);
            BinLogger.d("HTTP GET: " + url);
            return OkHttp.string(url, finalHeaders);
        } catch (Exception e) {
            BinLogger.e("HTTP GET 请求失败: " + url, e);
            return "";
        }
    }

    /**
     * GET 请求（带超时）
     * @param url 请求 URL
     * @param headers 请求头
     * @param timeout 超时时间（毫秒）
     * @return 响应内容
     */
    public static String get(String url, Map<String, String> headers, long timeout) {
        try {
            Map<String, String> finalHeaders = prepareHeaders(headers);
            BinLogger.d("HTTP GET: " + url + " (timeout: " + timeout + "ms)");
            return OkHttp.string(url, null, finalHeaders, timeout);
        } catch (Exception e) {
            BinLogger.e("HTTP GET 请求失败: " + url, e);
            return "";
        }
    }

    /**
     * POST 请求（表单数据）
     * @param url 请求 URL
     * @param params 表单参数
     * @return 响应内容
     */
    public static String post(String url, Map<String, String> params) {
        return post(url, params, null);
    }

    /**
     * POST 请求（表单数据，带请求头）
     * @param url 请求 URL
     * @param params 表单参数
     * @param headers 请求头
     * @return 响应内容
     */
    public static String post(String url, Map<String, String> params, Map<String, String> headers) {
        try {
            Map<String, String> finalHeaders = prepareHeaders(headers);
            BinLogger.d("HTTP POST: " + url);
            return OkHttp.post(url, params, finalHeaders).getBody();
        } catch (Exception e) {
            BinLogger.e("HTTP POST 请求失败: " + url, e);
            return "";
        }
    }

    /**
     * POST 请求（JSON 数据）
     * @param url 请求 URL
     * @param json JSON 字符串
     * @return 响应内容
     */
    public static String postJson(String url, String json) {
        return postJson(url, json, null);
    }

    /**
     * POST 请求（JSON 数据，带请求头）
     * @param url 请求 URL
     * @param json JSON 字符串
     * @param headers 请求头
     * @return 响应内容
     */
    public static String postJson(String url, String json, Map<String, String> headers) {
        try {
            Map<String, String> finalHeaders = prepareHeaders(headers);
            if (!finalHeaders.containsKey("Content-Type")) {
                finalHeaders.put("Content-Type", "application/json");
            }
            BinLogger.d("HTTP POST JSON: " + url);
            return OkHttp.post(url, json, finalHeaders).getBody();
        } catch (Exception e) {
            BinLogger.e("HTTP POST JSON 请求失败: " + url, e);
            return "";
        }
    }

    /**
     * 获取重定向后的 URL
     * @param url 原始 URL
     * @param headers 请求头
     * @return 重定向后的 URL
     */
    public static String getRedirectUrl(String url, Map<String, String> headers) {
        try {
            Map<String, String> finalHeaders = prepareHeaders(headers);
            return OkHttp.getLocation(url, finalHeaders);
        } catch (IOException e) {
            BinLogger.e("获取重定向 URL 失败: " + url, e);
            return url;
        }
    }

    /**
     * 拼接 URL
     * @param base 基础 URL
     * @param path 路径
     * @return 完整 URL
     */
    public static String joinUrl(String base, String path) {
        if (path == null || path.isEmpty()) {
            return base;
        }
        if (path.startsWith("http://") || path.startsWith("https://") || path.startsWith("//")) {
            return path;
        }
        if (base.endsWith("/")) {
            if (path.startsWith("/")) {
                return base + path.substring(1);
            }
            return base + path;
        } else {
            if (path.startsWith("/")) {
                // 拼接到域名根路径
                int index = base.indexOf("/", 8);
                if (index > 0) {
                    return base.substring(0, index) + path;
                }
                return base + path;
            }
            return base + "/" + path;
        }
    }

    /**
     * 准备请求头
     */
    private static Map<String, String> prepareHeaders(Map<String, String> headers) {
        Map<String, String> result = new HashMap<>();
        result.put("User-Agent", DEFAULT_USER_AGENT);
        if (headers != null) {
            result.putAll(headers);
        }
        return result;
    }

    /**
     * 将对象转换为 JSON
     */
    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    /**
     * 从 JSON 解析对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return gson.fromJson(json, clazz);
        } catch (Exception e) {
            BinLogger.e("JSON 解析失败", e);
            return null;
        }
    }
}
