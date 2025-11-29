package com.github.catvod.binrunner.config;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Settings configuration bean.
 * Contains all runtime settings for BinRunner.
 */
public class SettingsConfig {

    @SerializedName("log")
    private LogConfig log;

    @SerializedName("timeout")
    private int timeout;

    @SerializedName("maxProcesses")
    private int maxProcesses;

    @SerializedName("autoStart")
    private List<AutoStartConfig> autoStart;

    public SettingsConfig() {
        this.log = new LogConfig();
        this.timeout = 60;
        this.maxProcesses = 5;
        this.autoStart = new ArrayList<>();
    }

    public LogConfig getLog() {
        return log != null ? log : new LogConfig();
    }

    public void setLog(LogConfig log) {
        this.log = log;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getMaxProcesses() {
        return maxProcesses;
    }

    public void setMaxProcesses(int maxProcesses) {
        this.maxProcesses = maxProcesses;
    }

    public List<AutoStartConfig> getAutoStart() {
        return autoStart != null ? autoStart : new ArrayList<>();
    }

    public void setAutoStart(List<AutoStartConfig> autoStart) {
        this.autoStart = autoStart;
    }
}
