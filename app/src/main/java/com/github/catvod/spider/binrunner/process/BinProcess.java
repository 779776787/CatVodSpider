package com.github.catvod.spider.binrunner.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 进程封装类
 * 封装 Java Process，提供更便捷的进程管理功能
 */
public class BinProcess {

    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(0);

    private final int id;
    private final String name;
    private final String command;
    private final long startTime;
    private final boolean background;

    private Process process;
    private ProcessState state;
    private int restartCount;
    private volatile boolean cancelled;

    /**
     * 创建新的进程封装
     * @param name 进程名称
     * @param command 执行命令
     * @param background 是否后台运行
     */
    public BinProcess(String name, String command, boolean background) {
        this.id = ID_GENERATOR.incrementAndGet();
        this.name = name;
        this.command = command;
        this.background = background;
        this.startTime = System.currentTimeMillis();
        this.state = ProcessState.PENDING;
        this.restartCount = 0;
        this.cancelled = false;
    }

    /**
     * 获取进程 ID
     */
    public int getId() {
        return id;
    }

    /**
     * 获取进程名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取执行命令
     */
    public String getCommand() {
        return command;
    }

    /**
     * 获取启动时间
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * 是否后台运行
     */
    public boolean isBackground() {
        return background;
    }

    /**
     * 获取进程状态
     */
    public ProcessState getState() {
        return state;
    }

    /**
     * 设置进程状态
     */
    public void setState(ProcessState state) {
        this.state = state;
    }

    /**
     * 获取重启次数
     */
    public int getRestartCount() {
        return restartCount;
    }

    /**
     * 增加重启次数
     */
    public void incrementRestartCount() {
        this.restartCount++;
    }

    /**
     * 设置原生进程
     */
    public void setProcess(Process process) {
        this.process = process;
        this.state = ProcessState.RUNNING;
    }

    /**
     * 获取原生进程
     */
    public Process getProcess() {
        return process;
    }

    /**
     * 进程是否存活
     */
    public boolean isAlive() {
        return process != null && process.isAlive();
    }

    /**
     * 取消进程
     */
    public void cancel() {
        this.cancelled = true;
        destroy();
    }

    /**
     * 是否已取消
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * 销毁进程
     */
    public void destroy() {
        if (process != null) {
            process.destroy();
            try {
                if (!process.waitFor(3, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                }
            } catch (InterruptedException ignored) {
                process.destroyForcibly();
            }
        }
        if (state == ProcessState.RUNNING) {
            state = cancelled ? ProcessState.CANCELLED : ProcessState.COMPLETED;
        }
    }

    /**
     * 等待进程完成
     * @param timeout 超时时间（毫秒）
     * @return 进程是否在超时前完成
     */
    public boolean waitFor(long timeout) throws InterruptedException {
        if (process == null) {
            return true;
        }
        return process.waitFor(timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * 获取退出码
     */
    public int getExitCode() {
        if (process == null) {
            return -1;
        }
        try {
            return process.exitValue();
        } catch (IllegalThreadStateException e) {
            return -1;
        }
    }

    /**
     * 获取标准输入流
     */
    public OutputStream getOutputStream() {
        return process != null ? process.getOutputStream() : null;
    }

    /**
     * 获取标准输出流
     */
    public InputStream getInputStream() {
        return process != null ? process.getInputStream() : null;
    }

    /**
     * 获取标准错误流
     */
    public InputStream getErrorStream() {
        return process != null ? process.getErrorStream() : null;
    }

    /**
     * 读取标准输出
     */
    public String readOutput() throws IOException {
        return readStream(getInputStream());
    }

    /**
     * 读取标准错误
     */
    public String readError() throws IOException {
        return readStream(getErrorStream());
    }

    /**
     * 读取流内容
     */
    private String readStream(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString().trim();
    }

    /**
     * 获取运行时间（毫秒）
     */
    public long getRunningTime() {
        return System.currentTimeMillis() - startTime;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s (%s) - %s", id, name, command, state.getDescription());
    }
}
