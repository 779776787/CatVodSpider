package com.github.catvod.spider.binrunner.process;

/**
 * 进程状态枚举
 * 表示进程的运行状态
 */
public enum ProcessState {
    /**
     * 等待中
     */
    PENDING("pending", "等待中"),

    /**
     * 运行中
     */
    RUNNING("running", "运行中"),

    /**
     * 已完成
     */
    COMPLETED("completed", "已完成"),

    /**
     * 执行失败
     */
    FAILED("failed", "执行失败"),

    /**
     * 已取消
     */
    CANCELLED("cancelled", "已取消"),

    /**
     * 已超时
     */
    TIMEOUT("timeout", "已超时");

    private final String code;
    private final String description;

    ProcessState(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 获取状态代码
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取状态描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 根据代码获取状态
     */
    public static ProcessState fromCode(String code) {
        for (ProcessState state : values()) {
            if (state.code.equals(code)) {
                return state;
            }
        }
        return PENDING;
    }

    @Override
    public String toString() {
        return description;
    }
}
