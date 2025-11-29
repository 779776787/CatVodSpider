package com.github.catvod.spider.binrunner.config;

import com.google.gson.annotations.SerializedName;

/**
 * 自动启动配置类
 * 用于配置自动启动的后台进程
 */
public class AutoStartConfig {

    @SerializedName("name")
    private String name;

    @SerializedName("cmd")
    private String cmd;

    @SerializedName("background")
    private boolean background = true;

    @SerializedName("autoRestart")
    private boolean autoRestart = false;

    @SerializedName("maxRestarts")
    private int maxRestarts = 3;

    public AutoStartConfig() {
    }

    public AutoStartConfig(String name, String cmd) {
        this.name = name;
        this.cmd = cmd;
    }

    /**
     * 获取进程名称
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取执行命令
     */
    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    /**
     * 是否后台运行
     */
    public boolean isBackground() {
        return background;
    }

    public void setBackground(boolean background) {
        this.background = background;
    }

    /**
     * 是否自动重启
     */
    public boolean isAutoRestart() {
        return autoRestart;
    }

    public void setAutoRestart(boolean autoRestart) {
        this.autoRestart = autoRestart;
    }

    /**
     * 获取最大重启次数
     */
    public int getMaxRestarts() {
        return maxRestarts;
    }

    public void setMaxRestarts(int maxRestarts) {
        this.maxRestarts = maxRestarts;
    }
}
