package com.github.catvod.spider.binrunner.config;

import com.google.gson.annotations.SerializedName;

/**
 * 日志配置类
 * 用于管理日志的启用状态、存储路径和最大大小
 */
public class LogConfig {

    @SerializedName("enable")
    private boolean enable = true;

    @SerializedName("path")
    private String path = "/storage/emulated/0/TV/envs/logs";

    @SerializedName("maxSize")
    private int maxSize = 500;

    public LogConfig() {
    }

    public LogConfig(boolean enable, String path, int maxSize) {
        this.enable = enable;
        this.path = path;
        this.maxSize = maxSize;
    }

    /**
     * 是否启用日志
     */
    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    /**
     * 获取日志存储路径
     */
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 获取日志最大大小（KB）
     */
    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }
}
