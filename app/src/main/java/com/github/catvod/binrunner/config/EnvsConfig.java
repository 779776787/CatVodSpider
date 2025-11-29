package com.github.catvod.binrunner.config;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

/**
 * 环境变量配置类
 * 用于配置和管理各种解释器的环境变量
 */
public class EnvsConfig {

    @SerializedName("php")
    private Map<String, String> phpEnvs;

    @SerializedName("python")
    private Map<String, String> pythonEnvs;

    @SerializedName("node")
    private Map<String, String> nodeEnvs;

    @SerializedName("lua")
    private Map<String, String> luaEnvs;

    @SerializedName("common")
    private Map<String, String> commonEnvs;

    /**
     * 从 JSON 字符串解析配置
     * @param json JSON 字符串
     * @return 配置对象
     */
    public static EnvsConfig fromJson(String json) {
        if (json == null || json.isEmpty()) {
            return new EnvsConfig();
        }
        try {
            EnvsConfig config = new Gson().fromJson(json, EnvsConfig.class);
            return config != null ? config : new EnvsConfig();
        } catch (com.google.gson.JsonSyntaxException e) {
            // JSON 格式错误，返回默认配置
            return new EnvsConfig();
        }
    }

    /**
     * 获取 PHP 环境变量
     * @return PHP 环境变量
     */
    public Map<String, String> getPhpEnvs() {
        return phpEnvs != null ? phpEnvs : new HashMap<>();
    }

    /**
     * 设置 PHP 环境变量
     * @param phpEnvs PHP 环境变量
     */
    public void setPhpEnvs(Map<String, String> phpEnvs) {
        this.phpEnvs = phpEnvs;
    }

    /**
     * 获取 Python 环境变量
     * @return Python 环境变量
     */
    public Map<String, String> getPythonEnvs() {
        return pythonEnvs != null ? pythonEnvs : new HashMap<>();
    }

    /**
     * 设置 Python 环境变量
     * @param pythonEnvs Python 环境变量
     */
    public void setPythonEnvs(Map<String, String> pythonEnvs) {
        this.pythonEnvs = pythonEnvs;
    }

    /**
     * 获取 Node.js 环境变量
     * @return Node.js 环境变量
     */
    public Map<String, String> getNodeEnvs() {
        return nodeEnvs != null ? nodeEnvs : new HashMap<>();
    }

    /**
     * 设置 Node.js 环境变量
     * @param nodeEnvs Node.js 环境变量
     */
    public void setNodeEnvs(Map<String, String> nodeEnvs) {
        this.nodeEnvs = nodeEnvs;
    }

    /**
     * 获取 Lua 环境变量
     * @return Lua 环境变量
     */
    public Map<String, String> getLuaEnvs() {
        return luaEnvs != null ? luaEnvs : new HashMap<>();
    }

    /**
     * 设置 Lua 环境变量
     * @param luaEnvs Lua 环境变量
     */
    public void setLuaEnvs(Map<String, String> luaEnvs) {
        this.luaEnvs = luaEnvs;
    }

    /**
     * 获取通用环境变量
     * @return 通用环境变量
     */
    public Map<String, String> getCommonEnvs() {
        return commonEnvs != null ? commonEnvs : new HashMap<>();
    }

    /**
     * 设置通用环境变量
     * @param commonEnvs 通用环境变量
     */
    public void setCommonEnvs(Map<String, String> commonEnvs) {
        this.commonEnvs = commonEnvs;
    }

    /**
     * 根据解释器类型获取环境变量
     * @param type 解释器类型（php, python, node, lua）
     * @return 环境变量映射
     */
    public Map<String, String> getEnvsForType(String type) {
        Map<String, String> result = new HashMap<>();
        // 先添加通用环境变量
        result.putAll(getCommonEnvs());
        // 再添加特定类型的环境变量
        switch (type.toLowerCase()) {
            case "php":
                result.putAll(getPhpEnvs());
                break;
            case "python":
            case "py":
                result.putAll(getPythonEnvs());
                break;
            case "node":
            case "js":
            case "javascript":
                result.putAll(getNodeEnvs());
                break;
            case "lua":
                result.putAll(getLuaEnvs());
                break;
        }
        return result;
    }

    /**
     * 转换为 JSON 字符串
     * @return JSON 字符串
     */
    public String toJson() {
        return new Gson().toJson(this);
    }
}
