package com.github.catvod.spider.binrunner.runner;

import com.github.catvod.spider.binrunner.core.EnvManager;
import com.github.catvod.spider.binrunner.util.BinLogger;

/**
 * Runner 工厂类
 * 根据脚本扩展名创建对应的 Runner
 */
public class RunnerFactory {

    /**
     * 创建 Runner
     * @param envManager 环境管理器
     * @param spiderPath 爬虫脚本路径
     * @return 对应的 Runner，不支持的类型返回 null
     */
    public static BaseRunner create(EnvManager envManager, String spiderPath) {
        String ext = getExtension(spiderPath).toLowerCase();
        
        switch (ext) {
            case "js":
                // 判断是使用 QuickJS 还是 Node.js
                // 默认使用 QuickJS，除非路径中包含 node 或者有特殊标记
                if (spiderPath.contains("_node.") || spiderPath.contains("/node/")) {
                    BinLogger.d("创建 Node.js Runner: " + spiderPath);
                    return new NodeRunner(envManager, spiderPath);
                } else {
                    BinLogger.d("创建 QuickJS Runner: " + spiderPath);
                    return new JsRunner(envManager, spiderPath);
                }
                
            case "py":
            case "python":
                BinLogger.d("创建 Python Runner: " + spiderPath);
                return new PyRunner(envManager, spiderPath);
                
            case "php":
                BinLogger.d("创建 PHP Runner: " + spiderPath);
                return new PhpRunner(envManager, spiderPath);
                
            case "lua":
                BinLogger.d("创建 Lua Runner: " + spiderPath);
                return new LuaRunner(envManager, spiderPath);
                
            default:
                BinLogger.w("不支持的脚本类型: " + ext);
                return null;
        }
    }

    /**
     * 检查是否支持该脚本类型
     * @param spiderPath 脚本路径
     * @return 是否支持
     */
    public static boolean isSupported(String spiderPath) {
        String ext = getExtension(spiderPath).toLowerCase();
        return "js".equals(ext) || "py".equals(ext) || "python".equals(ext) ||
               "php".equals(ext) || "lua".equals(ext);
    }

    /**
     * 获取文件扩展名
     */
    private static String getExtension(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        int lastDot = path.lastIndexOf('.');
        if (lastDot > 0 && lastDot < path.length() - 1) {
            return path.substring(lastDot + 1);
        }
        return "";
    }

    /**
     * 根据扩展名获取解释器名称
     */
    public static String getInterpreterName(String extension) {
        switch (extension.toLowerCase()) {
            case "js":
                return "qjs";
            case "py":
            case "python":
                return "python3";
            case "php":
                return "php";
            case "lua":
                return "lua";
            default:
                return null;
        }
    }
}
