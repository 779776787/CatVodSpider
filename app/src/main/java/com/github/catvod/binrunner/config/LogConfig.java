package com.github.catvod.binrunner.config;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * 日志配置类
 * 用于配置日志输出行为
 */
public class LogConfig {

    /**
     * 是否启用日志
     */
    @SerializedName("enabled")
    private boolean enabled = true;

    /**
     * 日志级别（DEBUG, INFO, WARN, ERROR）
     */
    @SerializedName("level")
    private String level = "INFO";

    /**
     * 是否输出到文件
     */
    @SerializedName("fileOutput")
    private boolean fileOutput = false;

    /**
     * 日志文件路径
     */
    @SerializedName("filePath")
    private String filePath;

    /**
     * 最大日志文件大小（字节）
     */
    @SerializedName("maxFileSize")
    private long maxFileSize = 10 * 1024 * 1024; // 默认10MB

    /**
     * 是否显示时间戳
     */
    @SerializedName("showTimestamp")
    private boolean showTimestamp = true;

    /**
     * 日志时间格式
     */
    @SerializedName("timeFormat")
    private String timeFormat = "HH:mm:ss";

    /**
     * 从 JSON 字符串解析配置
     * @param json JSON 字符串
     * @return 配置对象
     */
    public static LogConfig fromJson(String json) {
        if (json == null || json.isEmpty()) {
            return new LogConfig();
        }
        try {
            LogConfig config = new Gson().fromJson(json, LogConfig.class);
            return config != null ? config : new LogConfig();
        } catch (com.google.gson.JsonSyntaxException e) {
            // JSON 格式错误，返回默认配置
            return new LogConfig();
        }
    }

    /**
     * 是否启用日志
     * @return 是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 设置是否启用日志
     * @param enabled 是否启用
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 获取日志级别
     * @return 日志级别
     */
    public String getLevel() {
        return level;
    }

    /**
     * 设置日志级别
     * @param level 日志级别
     */
    public void setLevel(String level) {
        this.level = level;
    }

    /**
     * 是否输出到文件
     * @return 是否输出到文件
     */
    public boolean isFileOutput() {
        return fileOutput;
    }

    /**
     * 设置是否输出到文件
     * @param fileOutput 是否输出到文件
     */
    public void setFileOutput(boolean fileOutput) {
        this.fileOutput = fileOutput;
    }

    /**
     * 获取日志文件路径
     * @return 日志文件路径
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * 设置日志文件路径
     * @param filePath 日志文件路径
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * 获取最大日志文件大小
     * @return 最大日志文件大小（字节）
     */
    public long getMaxFileSize() {
        return maxFileSize;
    }

    /**
     * 设置最大日志文件大小
     * @param maxFileSize 最大日志文件大小（字节）
     */
    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    /**
     * 是否显示时间戳
     * @return 是否显示时间戳
     */
    public boolean isShowTimestamp() {
        return showTimestamp;
    }

    /**
     * 设置是否显示时间戳
     * @param showTimestamp 是否显示时间戳
     */
    public void setShowTimestamp(boolean showTimestamp) {
        this.showTimestamp = showTimestamp;
    }

    /**
     * 获取时间格式
     * @return 时间格式
     */
    public String getTimeFormat() {
        return timeFormat;
    }

    /**
     * 设置时间格式
     * @param timeFormat 时间格式
     */
    public void setTimeFormat(String timeFormat) {
        this.timeFormat = timeFormat;
    }

    /**
     * 转换为 JSON 字符串
     * @return JSON 字符串
     */
    public String toJson() {
        return new Gson().toJson(this);
    }
}
