package com.github.catvod.binrunner.config;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * 自动启动配置类
 * 用于配置在初始化时自动执行的命令
 */
public class AutoStartConfig {

    /**
     * 是否启用自动启动
     */
    @SerializedName("enabled")
    private boolean enabled = false;

    /**
     * 自动启动命令列表
     */
    @SerializedName("commands")
    private List<AutoStartCommand> commands;

    /**
     * 从 JSON 字符串解析配置
     * @param json JSON 字符串
     * @return 配置对象
     */
    public static AutoStartConfig fromJson(String json) {
        try {
            return new Gson().fromJson(json, AutoStartConfig.class);
        } catch (Exception e) {
            return new AutoStartConfig();
        }
    }

    /**
     * 是否启用自动启动
     * @return 是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 设置是否启用自动启动
     * @param enabled 是否启用
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 获取自动启动命令列表
     * @return 命令列表
     */
    public List<AutoStartCommand> getCommands() {
        return commands != null ? commands : new ArrayList<>();
    }

    /**
     * 设置自动启动命令列表
     * @param commands 命令列表
     */
    public void setCommands(List<AutoStartCommand> commands) {
        this.commands = commands;
    }

    /**
     * 添加自动启动命令
     * @param command 命令
     */
    public void addCommand(AutoStartCommand command) {
        if (commands == null) {
            commands = new ArrayList<>();
        }
        commands.add(command);
    }

    /**
     * 转换为 JSON 字符串
     * @return JSON 字符串
     */
    public String toJson() {
        return new Gson().toJson(this);
    }

    /**
     * 自动启动命令
     */
    public static class AutoStartCommand {

        /**
         * 命令名称
         */
        @SerializedName("name")
        private String name;

        /**
         * 命令内容
         */
        @SerializedName("command")
        private String command;

        /**
         * 是否后台运行
         */
        @SerializedName("background")
        private boolean background = false;

        /**
         * 延迟执行时间（毫秒）
         */
        @SerializedName("delay")
        private long delay = 0;

        /**
         * 获取命令名称
         * @return 命令名称
         */
        public String getName() {
            return name;
        }

        /**
         * 设置命令名称
         * @param name 命令名称
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * 获取命令内容
         * @return 命令内容
         */
        public String getCommand() {
            return command;
        }

        /**
         * 设置命令内容
         * @param command 命令内容
         */
        public void setCommand(String command) {
            this.command = command;
        }

        /**
         * 是否后台运行
         * @return 是否后台运行
         */
        public boolean isBackground() {
            return background;
        }

        /**
         * 设置是否后台运行
         * @param background 是否后台运行
         */
        public void setBackground(boolean background) {
            this.background = background;
        }

        /**
         * 获取延迟执行时间
         * @return 延迟执行时间（毫秒）
         */
        public long getDelay() {
            return delay;
        }

        /**
         * 设置延迟执行时间
         * @param delay 延迟执行时间（毫秒）
         */
        public void setDelay(long delay) {
            this.delay = delay;
        }
    }
}
