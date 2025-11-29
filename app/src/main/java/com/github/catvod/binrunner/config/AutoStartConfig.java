package com.github.catvod.binrunner.config;

import com.google.gson.annotations.SerializedName;

/**
 * Auto-start configuration bean.
 * Defines a command to be executed automatically on startup.
 */
public class AutoStartConfig {

    @SerializedName("name")
    private String name;

    @SerializedName("cmd")
    private String cmd;

    @SerializedName("background")
    private boolean background;

    @SerializedName("autoRestart")
    private boolean autoRestart;

    @SerializedName("maxRestarts")
    private int maxRestarts;

    public AutoStartConfig() {
        this.name = "";
        this.cmd = "";
        this.background = false;
        this.autoRestart = false;
        this.maxRestarts = 3;
    }

    public String getName() {
        return name != null ? name : "";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCmd() {
        return cmd != null ? cmd : "";
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public boolean isBackground() {
        return background;
    }

    public void setBackground(boolean background) {
        this.background = background;
    }

    public boolean isAutoRestart() {
        return autoRestart;
    }

    public void setAutoRestart(boolean autoRestart) {
        this.autoRestart = autoRestart;
    }

    public int getMaxRestarts() {
        return maxRestarts > 0 ? maxRestarts : 3;
    }

    public void setMaxRestarts(int maxRestarts) {
        this.maxRestarts = maxRestarts;
    }
}
