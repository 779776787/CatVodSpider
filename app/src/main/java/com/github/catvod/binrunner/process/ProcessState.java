package com.github.catvod.binrunner.process;

/**
 * 进程状态枚举
 * 表示进程的当前运行状态
 */
public enum ProcessState {
    /**
     * 进程未启动
     */
    NOT_STARTED("未启动"),

    /**
     * 进程正在运行
     */
    RUNNING("运行中"),

    /**
     * 进程已完成
     */
    COMPLETED("已完成"),

    /**
     * 进程执行失败
     */
    FAILED("失败"),

    /**
     * 进程被终止
     */
    TERMINATED("已终止"),

    /**
     * 进程超时
     */
    TIMEOUT("超时");

    private final String description;

    ProcessState(String description) {
        this.description = description;
    }

    /**
     * 获取状态描述
     * @return 状态描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 判断进程是否已结束
     * @return 是否已结束
     */
    public boolean isFinished() {
        return this == COMPLETED || this == FAILED || this == TERMINATED || this == TIMEOUT;
    }

    /**
     * 判断进程是否正在运行
     * @return 是否正在运行
     */
    public boolean isRunning() {
        return this == RUNNING;
    }
}
