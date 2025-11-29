package com.github.catvod.binrunner.config;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * 通用设置配置类
 * 用于配置 BinRunner 的全局设置
 */
public class SettingsConfig {

    /**
     * 默认执行超时时间（毫秒）
     */
    @SerializedName("timeout")
    private long timeout = 30000;

    /**
     * 是否启用日志
     */
    @SerializedName("enableLog")
    private boolean enableLog = true;

    /**
     * 日志级别（debug, info, warn, error）
     */
    @SerializedName("logLevel")
    private String logLevel = "info";

    /**
     * 工作目录
     */
    @SerializedName("workDir")
    private String workDir;

    /**
     * 二进制文件目录
     */
    @SerializedName("binDir")
    private String binDir;

    /**
     * 库文件目录
     */
    @SerializedName("libDir")
    private String libDir;

    /**
     * PHP 解释器路径
     */
    @SerializedName("phpPath")
    private String phpPath;

    /**
     * Python 解释器路径
     */
    @SerializedName("pythonPath")
    private String pythonPath;

    /**
     * Node.js 解释器路径
     */
    @SerializedName("nodePath")
    private String nodePath;

    /**
     * Lua 解释器路径
     */
    @SerializedName("luaPath")
    private String luaPath;

    /**
     * 从 JSON 字符串解析配置
     * @param json JSON 字符串
     * @return 配置对象
     */
    public static SettingsConfig fromJson(String json) {
        try {
            return new Gson().fromJson(json, SettingsConfig.class);
        } catch (Exception e) {
            return new SettingsConfig();
        }
    }

    /**
     * 获取超时时间
     * @return 超时时间（毫秒）
     */
    public long getTimeout() {
        return timeout;
    }

    /**
     * 设置超时时间
     * @param timeout 超时时间（毫秒）
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /**
     * 是否启用日志
     * @return 是否启用
     */
    public boolean isEnableLog() {
        return enableLog;
    }

    /**
     * 设置是否启用日志
     * @param enableLog 是否启用
     */
    public void setEnableLog(boolean enableLog) {
        this.enableLog = enableLog;
    }

    /**
     * 获取日志级别
     * @return 日志级别
     */
    public String getLogLevel() {
        return logLevel;
    }

    /**
     * 设置日志级别
     * @param logLevel 日志级别
     */
    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    /**
     * 获取工作目录
     * @return 工作目录
     */
    public String getWorkDir() {
        return workDir;
    }

    /**
     * 设置工作目录
     * @param workDir 工作目录
     */
    public void setWorkDir(String workDir) {
        this.workDir = workDir;
    }

    /**
     * 获取二进制文件目录
     * @return 二进制文件目录
     */
    public String getBinDir() {
        return binDir;
    }

    /**
     * 设置二进制文件目录
     * @param binDir 二进制文件目录
     */
    public void setBinDir(String binDir) {
        this.binDir = binDir;
    }

    /**
     * 获取库文件目录
     * @return 库文件目录
     */
    public String getLibDir() {
        return libDir;
    }

    /**
     * 设置库文件目录
     * @param libDir 库文件目录
     */
    public void setLibDir(String libDir) {
        this.libDir = libDir;
    }

    /**
     * 获取 PHP 解释器路径
     * @return PHP 解释器路径
     */
    public String getPhpPath() {
        return phpPath;
    }

    /**
     * 设置 PHP 解释器路径
     * @param phpPath PHP 解释器路径
     */
    public void setPhpPath(String phpPath) {
        this.phpPath = phpPath;
    }

    /**
     * 获取 Python 解释器路径
     * @return Python 解释器路径
     */
    public String getPythonPath() {
        return pythonPath;
    }

    /**
     * 设置 Python 解释器路径
     * @param pythonPath Python 解释器路径
     */
    public void setPythonPath(String pythonPath) {
        this.pythonPath = pythonPath;
    }

    /**
     * 获取 Node.js 解释器路径
     * @return Node.js 解释器路径
     */
    public String getNodePath() {
        return nodePath;
    }

    /**
     * 设置 Node.js 解释器路径
     * @param nodePath Node.js 解释器路径
     */
    public void setNodePath(String nodePath) {
        this.nodePath = nodePath;
    }

    /**
     * 获取 Lua 解释器路径
     * @return Lua 解释器路径
     */
    public String getLuaPath() {
        return luaPath;
    }

    /**
     * 设置 Lua 解释器路径
     * @param luaPath Lua 解释器路径
     */
    public void setLuaPath(String luaPath) {
        this.luaPath = luaPath;
    }

    /**
     * 转换为 JSON 字符串
     * @return JSON 字符串
     */
    public String toJson() {
        return new Gson().toJson(this);
    }
}
