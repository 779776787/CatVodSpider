package com.github.catvod.spider.binrunner.config;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

/**
 * 环境配置总类
 * 包含全局设置和别名配置
 */
public class EnvsConfig {

    private static final String DEFAULT_CONFIG = "{\"settings\":{\"log\":{\"enable\":true,\"path\":\"/storage/emulated/0/TV/envs/logs\",\"maxSize\":500},\"timeout\":60,\"maxProcesses\":5,\"autoStart\":[]},\"alias\":{\"php\":\"php80\",\"py\":\"python3\",\"js\":\"qjs\",\"node\":\"node\"}}";

    @SerializedName("settings")
    private SettingsConfig settings = new SettingsConfig();

    @SerializedName("alias")
    private Map<String, String> alias = new HashMap<>();

    public EnvsConfig() {
        // 默认别名配置
        alias.put("php", "php80");
        alias.put("py", "python3");
        alias.put("js", "qjs");
        alias.put("node", "node");
    }

    /**
     * 从 JSON 字符串解析配置
     */
    public static EnvsConfig fromJson(String json) {
        try {
            return new Gson().fromJson(json, EnvsConfig.class);
        } catch (Exception e) {
            return new EnvsConfig();
        }
    }

    /**
     * 获取默认配置
     */
    public static EnvsConfig getDefault() {
        return fromJson(DEFAULT_CONFIG);
    }

    /**
     * 获取设置配置
     */
    public SettingsConfig getSettings() {
        return settings;
    }

    public void setSettings(SettingsConfig settings) {
        this.settings = settings;
    }

    /**
     * 获取别名映射
     */
    public Map<String, String> getAlias() {
        return alias;
    }

    public void setAlias(Map<String, String> alias) {
        this.alias = alias;
    }

    /**
     * 根据别名获取实际的二进制名称
     * @param name 别名或原始名称
     * @return 实际的二进制名称
     */
    public String resolveAlias(String name) {
        return alias.getOrDefault(name, name);
    }

    /**
     * 转换为 JSON 字符串
     */
    public String toJson() {
        return new Gson().toJson(this);
    }
}
