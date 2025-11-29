package com.github.catvod.spider.binrunner.bridge;

import com.github.catvod.spider.Proxy;
import com.github.catvod.spider.binrunner.util.BinLogger;
import com.github.catvod.utils.Prefers;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * HTTP 桥接服务类
 * 为 QuickJS 等无法直接注入 Java 方法的脚本提供 HTTP 服务桥接
 */
public class BinBridge {

    private static final Gson gson = new Gson();
    private static BinBridge instance;

    private ServerSocket serverSocket;
    private ExecutorService executor;
    private AtomicBoolean running;
    private int port;

    // 本地缓存
    private final Map<String, String> localCache = new HashMap<>();

    private BinBridge() {
        this.running = new AtomicBoolean(false);
    }

    /**
     * 获取单例实例
     */
    public static synchronized BinBridge getInstance() {
        if (instance == null) {
            instance = new BinBridge();
        }
        return instance;
    }

    /**
     * 启动桥接服务
     * @return 服务端口号
     */
    public int start() {
        if (running.get()) {
            return port;
        }

        try {
            // 查找可用端口
            serverSocket = new ServerSocket(0);
            port = serverSocket.getLocalPort();
            
            executor = Executors.newCachedThreadPool();
            running.set(true);

            // 启动接受连接的线程
            new Thread(this::acceptConnections).start();

            BinLogger.i("桥接服务已启动，端口: " + port);
            return port;

        } catch (Exception e) {
            BinLogger.e("启动桥接服务失败", e);
            return -1;
        }
    }

    /**
     * 停止桥接服务
     */
    public void stop() {
        running.set(false);
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (Exception e) {
            // 忽略
        }
        if (executor != null) {
            executor.shutdownNow();
        }
        BinLogger.i("桥接服务已停止");
    }

    /**
     * 获取服务端口
     */
    public int getPort() {
        return port;
    }

    /**
     * 是否正在运行
     */
    public boolean isRunning() {
        return running.get();
    }

    /**
     * 接受连接
     */
    private void acceptConnections() {
        while (running.get()) {
            try {
                Socket socket = serverSocket.accept();
                executor.submit(() -> handleRequest(socket));
            } catch (Exception e) {
                if (running.get()) {
                    BinLogger.e("接受连接失败", e);
                }
            }
        }
    }

    /**
     * 处理请求
     */
    private void handleRequest(Socket socket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             OutputStream out = socket.getOutputStream()) {

            // 读取请求行
            String requestLine = reader.readLine();
            if (requestLine == null) {
                return;
            }

            // 解析请求
            String[] parts = requestLine.split(" ");
            if (parts.length < 2) {
                return;
            }

            String method = parts[0];
            String path = parts[1];

            // 读取请求头
            Map<String, String> headers = new HashMap<>();
            String line;
            int contentLength = 0;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                int colonIndex = line.indexOf(':');
                if (colonIndex > 0) {
                    String key = line.substring(0, colonIndex).trim();
                    String value = line.substring(colonIndex + 1).trim();
                    headers.put(key.toLowerCase(), value);
                    if (key.equalsIgnoreCase("content-length")) {
                        contentLength = Integer.parseInt(value);
                    }
                }
            }

            // 读取请求体
            String body = "";
            if (contentLength > 0) {
                char[] buffer = new char[contentLength];
                reader.read(buffer, 0, contentLength);
                body = new String(buffer);
            }

            // 处理请求
            String response = processRequest(path, body);

            // 发送响应
            String httpResponse = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: application/json; charset=utf-8\r\n" +
                    "Content-Length: " + response.getBytes(StandardCharsets.UTF_8).length + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n" +
                    response;

            out.write(httpResponse.getBytes(StandardCharsets.UTF_8));
            out.flush();

        } catch (Exception e) {
            BinLogger.e("处理请求失败", e);
        } finally {
            try {
                socket.close();
            } catch (Exception e) {
                // 忽略
            }
        }
    }

    /**
     * 处理请求路径
     */
    private String processRequest(String path, String body) {
        try {
            // 解析路径参数
            String action = path;
            if (path.contains("?")) {
                action = path.substring(0, path.indexOf("?"));
            }
            if (action.startsWith("/")) {
                action = action.substring(1);
            }

            JsonObject request = body != null && !body.isEmpty() ? gson.fromJson(body, JsonObject.class) : new JsonObject();
            JsonObject response = new JsonObject();

            switch (action) {
                case "http":
                    return handleHttp(request);
                case "local.get":
                    return handleLocalGet(request);
                case "local.set":
                    return handleLocalSet(request);
                case "local.delete":
                    return handleLocalDelete(request);
                case "md5":
                    return handleMd5(request);
                case "aes.encrypt":
                    return handleAesEncrypt(request);
                case "aes.decrypt":
                    return handleAesDecrypt(request);
                case "rsa.encrypt":
                    return handleRsaEncrypt(request);
                case "rsa.decrypt":
                    return handleRsaDecrypt(request);
                case "s2t":
                    return handleS2T(request);
                case "t2s":
                    return handleT2S(request);
                case "getPort":
                    return handleGetPort();
                case "getProxy":
                    return handleGetProxy();
                case "joinUrl":
                    return handleJoinUrl(request);
                default:
                    response.addProperty("error", "未知操作: " + action);
                    return gson.toJson(response);
            }

        } catch (Exception e) {
            JsonObject error = new JsonObject();
            error.addProperty("error", e.getMessage());
            return gson.toJson(error);
        }
    }

    /**
     * 处理 HTTP 请求
     */
    private String handleHttp(JsonObject request) {
        String url = request.has("url") ? request.get("url").getAsString() : "";
        String method = request.has("method") ? request.get("method").getAsString() : "GET";
        
        Map<String, String> headers = new HashMap<>();
        if (request.has("headers") && request.get("headers").isJsonObject()) {
            JsonObject h = request.getAsJsonObject("headers");
            for (String key : h.keySet()) {
                headers.put(key, h.get(key).getAsString());
            }
        }

        String result;
        if ("POST".equalsIgnoreCase(method)) {
            String data = request.has("data") ? request.get("data").getAsString() : "";
            if (request.has("json") && request.get("json").getAsBoolean()) {
                result = BinConnect.postJson(url, data, headers);
            } else {
                result = BinConnect.post(url, parseParams(data), headers);
            }
        } else {
            result = BinConnect.get(url, headers);
        }

        JsonObject response = new JsonObject();
        response.addProperty("data", result);
        return gson.toJson(response);
    }

    /**
     * 解析表单参数
     */
    private Map<String, String> parseParams(String data) {
        Map<String, String> params = new HashMap<>();
        if (data != null && !data.isEmpty()) {
            for (String pair : data.split("&")) {
                String[] kv = pair.split("=", 2);
                if (kv.length == 2) {
                    params.put(kv[0], kv[1]);
                }
            }
        }
        return params;
    }

    /**
     * 处理本地缓存读取
     */
    private String handleLocalGet(JsonObject request) {
        String key = request.has("key") ? request.get("key").getAsString() : "";
        String defaultValue = request.has("default") ? request.get("default").getAsString() : "";
        
        // 优先从内存缓存读取，然后从 SharedPreferences 读取
        String value = localCache.containsKey(key) ? localCache.get(key) : Prefers.getString("binrunner_" + key, defaultValue);
        
        JsonObject response = new JsonObject();
        response.addProperty("value", value);
        return gson.toJson(response);
    }

    /**
     * 处理本地缓存写入
     */
    private String handleLocalSet(JsonObject request) {
        String key = request.has("key") ? request.get("key").getAsString() : "";
        String value = request.has("value") ? request.get("value").getAsString() : "";
        
        // 写入内存缓存和 SharedPreferences
        localCache.put(key, value);
        Prefers.put("binrunner_" + key, value);
        
        JsonObject response = new JsonObject();
        response.addProperty("success", true);
        return gson.toJson(response);
    }

    /**
     * 处理本地缓存删除
     */
    private String handleLocalDelete(JsonObject request) {
        String key = request.has("key") ? request.get("key").getAsString() : "";
        
        localCache.remove(key);
        Prefers.put("binrunner_" + key, "");
        
        JsonObject response = new JsonObject();
        response.addProperty("success", true);
        return gson.toJson(response);
    }

    /**
     * 处理 MD5
     */
    private String handleMd5(JsonObject request) {
        String data = request.has("data") ? request.get("data").getAsString() : "";
        JsonObject response = new JsonObject();
        response.addProperty("result", BinCrypto.md5(data));
        return gson.toJson(response);
    }

    /**
     * 处理 AES 加密
     */
    private String handleAesEncrypt(JsonObject request) {
        String data = request.has("data") ? request.get("data").getAsString() : "";
        String key = request.has("key") ? request.get("key").getAsString() : "";
        String iv = request.has("iv") ? request.get("iv").getAsString() : "";
        
        JsonObject response = new JsonObject();
        if (iv.isEmpty()) {
            response.addProperty("result", BinCrypto.aesEcbEncrypt(data, key));
        } else {
            response.addProperty("result", BinCrypto.aesEncrypt(data, key, iv));
        }
        return gson.toJson(response);
    }

    /**
     * 处理 AES 解密
     */
    private String handleAesDecrypt(JsonObject request) {
        String data = request.has("data") ? request.get("data").getAsString() : "";
        String key = request.has("key") ? request.get("key").getAsString() : "";
        String iv = request.has("iv") ? request.get("iv").getAsString() : "";
        
        JsonObject response = new JsonObject();
        if (iv.isEmpty()) {
            response.addProperty("result", BinCrypto.aesEcbDecrypt(data, key));
        } else {
            response.addProperty("result", BinCrypto.aesDecrypt(data, key, iv));
        }
        return gson.toJson(response);
    }

    /**
     * 处理 RSA 加密
     */
    private String handleRsaEncrypt(JsonObject request) {
        String data = request.has("data") ? request.get("data").getAsString() : "";
        String key = request.has("key") ? request.get("key").getAsString() : "";
        
        JsonObject response = new JsonObject();
        response.addProperty("result", BinCrypto.rsaEncrypt(data, key));
        return gson.toJson(response);
    }

    /**
     * 处理 RSA 解密
     */
    private String handleRsaDecrypt(JsonObject request) {
        String data = request.has("data") ? request.get("data").getAsString() : "";
        String key = request.has("key") ? request.get("key").getAsString() : "";
        
        JsonObject response = new JsonObject();
        response.addProperty("result", BinCrypto.rsaDecrypt(data, key));
        return gson.toJson(response);
    }

    /**
     * 处理简体转繁体
     */
    private String handleS2T(JsonObject request) {
        String text = request.has("text") ? request.get("text").getAsString() : "";
        JsonObject response = new JsonObject();
        response.addProperty("result", BinTrans.s2t(text));
        return gson.toJson(response);
    }

    /**
     * 处理繁体转简体
     */
    private String handleT2S(JsonObject request) {
        String text = request.has("text") ? request.get("text").getAsString() : "";
        JsonObject response = new JsonObject();
        response.addProperty("result", BinTrans.t2s(text));
        return gson.toJson(response);
    }

    /**
     * 获取代理端口
     */
    private String handleGetPort() {
        JsonObject response = new JsonObject();
        response.addProperty("port", Proxy.getPort());
        return gson.toJson(response);
    }

    /**
     * 获取代理地址
     */
    private String handleGetProxy() {
        JsonObject response = new JsonObject();
        response.addProperty("url", Proxy.getUrl());
        return gson.toJson(response);
    }

    /**
     * 处理 URL 拼接
     */
    private String handleJoinUrl(JsonObject request) {
        String base = request.has("base") ? request.get("base").getAsString() : "";
        String path = request.has("path") ? request.get("path").getAsString() : "";
        
        JsonObject response = new JsonObject();
        response.addProperty("url", BinConnect.joinUrl(base, path));
        return gson.toJson(response);
    }
}
