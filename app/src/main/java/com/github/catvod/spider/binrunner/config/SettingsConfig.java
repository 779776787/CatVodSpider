package com.github.catvod.spider.binrunner.config;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * 设置配置类
 * 包含日志、超时、进程数限制和自动启动配置
 */
public class SettingsConfig {

    @SerializedName("log")
    private LogConfig log = new LogConfig();

    @SerializedName("timeout")
    private int timeout = 60;

    @SerializedName("maxProcesses")
    private int maxProcesses = 5;

    @SerializedName("autoStart")
    private List<AutoStartConfig> autoStart = new ArrayList<>();

    public SettingsConfig() {
    }

    /**
     * 获取日志配置
     */
    public LogConfig getLog() {
        return log;
    }

    public void setLog(LogConfig log) {
        this.log = log;
    }

    /**
     * 获取超时时间（秒）
     */
    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * 获取最大进程数
     */
    public int getMaxProcesses() {
        return maxProcesses;
    }

    public void setMaxProcesses(int maxProcesses) {
        this.maxProcesses = maxProcesses;
    }

    /**
     * 获取自动启动配置列表
     */
    public List<AutoStartConfig> getAutoStart() {
        return autoStart;
    }

    public void setAutoStart(List<AutoStartConfig> autoStart) {
        this.autoStart = autoStart;
    }
}
