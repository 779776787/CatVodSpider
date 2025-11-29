package com.github.catvod.binrunner.config;

import com.google.gson.annotations.SerializedName;

/**
 * Log configuration bean.
 * Controls logging behavior including enable/disable, path, and max size.
 */
public class LogConfig {

    @SerializedName("enable")
    private boolean enable;

    @SerializedName("path")
    private String path;

    @SerializedName("maxSize")
    private int maxSize;

    public LogConfig() {
        this.enable = false;
        this.path = "";
        this.maxSize = 500;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getMaxSize() {
        return maxSize > 0 ? maxSize : 500;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }
}
