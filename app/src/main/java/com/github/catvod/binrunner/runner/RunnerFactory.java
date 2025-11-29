package com.github.catvod.binrunner.runner;

import com.github.catvod.binrunner.core.EnvManager;
import com.github.catvod.binrunner.util.BinLogger;
import com.github.catvod.binrunner.util.FileHelper;

/**
 * Runner 工厂类
 * 根据脚本类型创建对应的 Runner
 */
public class RunnerFactory {

    /**
     * 根据脚本路径创建 Runner
     * @param scriptPath 脚本文件路径
     * @param envManager 环境管理器
     * @return Runner 实例，不支持的类型返回 null
     */
    public static BaseRunner create(String scriptPath, EnvManager envManager) {
        if (scriptPath == null || scriptPath.isEmpty()) {
            BinLogger.error("脚本路径不能为空");
            return null;
        }

        String extension = FileHelper.getExtension(scriptPath);
        if (extension.isEmpty()) {
            BinLogger.error("无法识别脚本类型: " + scriptPath);
            return null;
        }

        BaseRunner runner = createByExtension(extension, scriptPath, envManager);
        if (runner == null) {
            BinLogger.error("不支持的脚本类型: " + extension);
        } else {
            BinLogger.info("创建 " + extension.toUpperCase() + " Runner: " + scriptPath);
        }

        return runner;
    }

    /**
     * 根据扩展名创建 Runner
     * @param extension 扩展名
     * @param scriptPath 脚本路径
     * @param envManager 环境管理器
     * @return Runner 实例
     */
    private static BaseRunner createByExtension(String extension, String scriptPath, EnvManager envManager) {
        switch (extension.toLowerCase()) {
            case "php":
                return new PhpRunner(scriptPath, envManager);
            case "py":
                return new PyRunner(scriptPath, envManager);
            case "js":
                return new JsRunner(scriptPath, envManager);
            case "lua":
                return new LuaRunner(scriptPath, envManager);
            default:
                return null;
        }
    }

    /**
     * 判断是否支持的脚本类型
     * @param scriptPath 脚本路径
     * @return 是否支持
     */
    public static boolean isSupported(String scriptPath) {
        if (scriptPath == null || scriptPath.isEmpty()) {
            return false;
        }
        String extension = FileHelper.getExtension(scriptPath);
        return isSupportedExtension(extension);
    }

    /**
     * 判断扩展名是否支持
     * @param extension 扩展名
     * @return 是否支持
     */
    public static boolean isSupportedExtension(String extension) {
        if (extension == null || extension.isEmpty()) {
            return false;
        }
        switch (extension.toLowerCase()) {
            case "php":
            case "py":
            case "js":
            case "lua":
                return true;
            default:
                return false;
        }
    }

    /**
     * 获取脚本类型描述
     * @param extension 扩展名
     * @return 类型描述
     */
    public static String getTypeDescription(String extension) {
        if (extension == null || extension.isEmpty()) {
            return "未知";
        }
        switch (extension.toLowerCase()) {
            case "php":
                return "PHP 脚本";
            case "py":
                return "Python 脚本";
            case "js":
                return "JavaScript 脚本";
            case "lua":
                return "Lua 脚本";
            default:
                return "未知类型";
        }
    }
}
