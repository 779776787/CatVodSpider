package com.github.catvod.binrunner.core;

import android.content.Context;

import com.github.catvod.binrunner.config.EnvsConfig;
import com.github.catvod.binrunner.config.SettingsConfig;
import com.github.catvod.binrunner.util.BinLogger;
import com.github.catvod.binrunner.util.FileHelper;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 环境管理器
 * 负责管理解释器路径和环境变量
 */
public class EnvManager {

    private static volatile EnvManager instance;

    private Context context;
    private SettingsConfig settings;
    private EnvsConfig envsConfig;
    private final Map<String, String> envMap;
    private String workDir;
    private String binDir;
    private String libDir;

    // 解释器路径
    private String phpPath;
    private String pythonPath;
    private String nodePath;
    private String luaPath;

    private EnvManager() {
        envMap = new HashMap<>();
    }

    /**
     * 获取单例实例
     * @return 环境管理器实例
     */
    public static EnvManager getInstance() {
        if (instance == null) {
            synchronized (EnvManager.class) {
                if (instance == null) {
                    instance = new EnvManager();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化环境管理器
     * @param context 上下文
     */
    public void init(Context context) {
        this.context = context;
        this.settings = new SettingsConfig();
        this.envsConfig = new EnvsConfig();

        // 初始化默认目录
        File filesDir = context.getFilesDir();
        workDir = filesDir.getAbsolutePath();
        binDir = new File(filesDir, "bin").getAbsolutePath();
        libDir = new File(filesDir, "lib").getAbsolutePath();

        // 确保目录存在
        FileHelper.ensureDirectory(binDir);
        FileHelper.ensureDirectory(libDir);

        // 初始化环境变量
        initEnvMap();

        // 自动检测解释器
        detectInterpreters();

        BinLogger.info("环境管理器初始化完成");
    }

    /**
     * 使用配置初始化
     * @param context 上下文
     * @param settings 设置配置
     * @param envsConfig 环境变量配置
     */
    public void init(Context context, SettingsConfig settings, EnvsConfig envsConfig) {
        this.context = context;
        this.settings = settings != null ? settings : new SettingsConfig();
        this.envsConfig = envsConfig != null ? envsConfig : new EnvsConfig();

        // 从配置中获取目录
        if (this.settings.getWorkDir() != null) {
            workDir = this.settings.getWorkDir();
        } else {
            workDir = context.getFilesDir().getAbsolutePath();
        }

        if (this.settings.getBinDir() != null) {
            binDir = this.settings.getBinDir();
        } else {
            binDir = new File(context.getFilesDir(), "bin").getAbsolutePath();
        }

        if (this.settings.getLibDir() != null) {
            libDir = this.settings.getLibDir();
        } else {
            libDir = new File(context.getFilesDir(), "lib").getAbsolutePath();
        }

        // 确保目录存在
        FileHelper.ensureDirectory(binDir);
        FileHelper.ensureDirectory(libDir);

        // 从配置中获取解释器路径
        phpPath = this.settings.getPhpPath();
        pythonPath = this.settings.getPythonPath();
        nodePath = this.settings.getNodePath();
        luaPath = this.settings.getLuaPath();

        // 初始化环境变量
        initEnvMap();

        // 自动检测解释器
        detectInterpreters();

        BinLogger.info("环境管理器初始化完成（使用自定义配置）");
    }

    /**
     * 初始化环境变量映射
     */
    private void initEnvMap() {
        envMap.clear();

        // 添加 PATH
        String path = System.getenv("PATH");
        if (path == null) path = "";
        path = binDir + ":" + path;
        envMap.put("PATH", path);

        // 添加 LD_LIBRARY_PATH
        String ldPath = System.getenv("LD_LIBRARY_PATH");
        if (ldPath == null) ldPath = "";
        ldPath = libDir + ":" + ldPath;
        envMap.put("LD_LIBRARY_PATH", ldPath);

        // 添加 HOME
        envMap.put("HOME", workDir);

        // 添加 TMPDIR
        String tmpDir = new File(workDir, "tmp").getAbsolutePath();
        FileHelper.ensureDirectory(tmpDir);
        envMap.put("TMPDIR", tmpDir);

        // 合并配置中的通用环境变量
        if (envsConfig != null) {
            envMap.putAll(envsConfig.getCommonEnvs());
        }
    }

    /**
     * 自动检测解释器
     */
    private void detectInterpreters() {
        // 检测 PHP
        if (phpPath == null || !FileHelper.isExecutable(phpPath)) {
            phpPath = detectInterpreter("php80", "php8", "php");
        }

        // 检测 Python
        if (pythonPath == null || !FileHelper.isExecutable(pythonPath)) {
            pythonPath = detectInterpreter("python3", "python");
        }

        // 检测 Node.js
        if (nodePath == null || !FileHelper.isExecutable(nodePath)) {
            nodePath = detectInterpreter("node", "nodejs");
        }

        // 检测 Lua
        if (luaPath == null || !FileHelper.isExecutable(luaPath)) {
            luaPath = detectInterpreter("lua", "lua5.4", "lua5.3");
        }

        BinLogger.debug("PHP 路径: " + (phpPath != null ? phpPath : "未找到"));
        BinLogger.debug("Python 路径: " + (pythonPath != null ? pythonPath : "未找到"));
        BinLogger.debug("Node.js 路径: " + (nodePath != null ? nodePath : "未找到"));
        BinLogger.debug("Lua 路径: " + (luaPath != null ? luaPath : "未找到"));
    }

    /**
     * 检测解释器路径
     * @param names 解释器名称列表
     * @return 解释器路径，未找到返回 null
     */
    private String detectInterpreter(String... names) {
        for (String name : names) {
            // 先在 binDir 中查找
            String path = new File(binDir, name).getAbsolutePath();
            if (FileHelper.isExecutable(path)) {
                return path;
            }

            // 在系统 PATH 中查找
            String systemPath = System.getenv("PATH");
            if (systemPath != null) {
                for (String dir : systemPath.split(":")) {
                    path = new File(dir, name).getAbsolutePath();
                    if (FileHelper.isExecutable(path)) {
                        return path;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 获取环境变量映射
     * @return 环境变量映射
     */
    public Map<String, String> getEnvMap() {
        return new HashMap<>(envMap);
    }

    /**
     * 获取指定类型的环境变量
     * @param type 解释器类型
     * @return 环境变量映射
     */
    public Map<String, String> getEnvMapForType(String type) {
        Map<String, String> result = new HashMap<>(envMap);
        if (envsConfig != null) {
            result.putAll(envsConfig.getEnvsForType(type));
        }
        return result;
    }

    /**
     * 获取 PHP 解释器路径
     * @return PHP 路径
     */
    public String getPhpPath() {
        return phpPath;
    }

    /**
     * 设置 PHP 解释器路径
     * @param phpPath PHP 路径
     */
    public void setPhpPath(String phpPath) {
        this.phpPath = phpPath;
    }

    /**
     * 获取 Python 解释器路径
     * @return Python 路径
     */
    public String getPythonPath() {
        return pythonPath;
    }

    /**
     * 设置 Python 解释器路径
     * @param pythonPath Python 路径
     */
    public void setPythonPath(String pythonPath) {
        this.pythonPath = pythonPath;
    }

    /**
     * 获取 Node.js 解释器路径
     * @return Node.js 路径
     */
    public String getNodePath() {
        return nodePath;
    }

    /**
     * 设置 Node.js 解释器路径
     * @param nodePath Node.js 路径
     */
    public void setNodePath(String nodePath) {
        this.nodePath = nodePath;
    }

    /**
     * 获取 Lua 解释器路径
     * @return Lua 路径
     */
    public String getLuaPath() {
        return luaPath;
    }

    /**
     * 设置 Lua 解释器路径
     * @param luaPath Lua 路径
     */
    public void setLuaPath(String luaPath) {
        this.luaPath = luaPath;
    }

    /**
     * 获取工作目录
     * @return 工作目录
     */
    public String getWorkDir() {
        return workDir;
    }

    /**
     * 获取二进制目录
     * @return 二进制目录
     */
    public String getBinDir() {
        return binDir;
    }

    /**
     * 获取库目录
     * @return 库目录
     */
    public String getLibDir() {
        return libDir;
    }

    /**
     * 获取上下文
     * @return 上下文
     */
    public Context getContext() {
        return context;
    }

    /**
     * 获取设置配置
     * @return 设置配置
     */
    public SettingsConfig getSettings() {
        return settings;
    }

    /**
     * 根据脚本扩展名获取解释器路径
     * @param extension 扩展名
     * @return 解释器路径
     */
    public String getInterpreterForExtension(String extension) {
        if (extension == null) return null;
        switch (extension.toLowerCase()) {
            case "php":
                return phpPath;
            case "py":
            case "python":
                return pythonPath;
            case "js":
            case "javascript":
                return nodePath;
            case "lua":
                return luaPath;
            default:
                return null;
        }
    }
}
