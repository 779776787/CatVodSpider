package com.github.catvod.binrunner.process;

import com.github.catvod.binrunner.util.BinLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * 进程封装类
 * 封装系统进程，提供统一的执行和管理接口
 */
public class BinProcess {

    private Process process;
    private ProcessState state;
    private final String[] command;
    private final String[] envp;
    private final File workDir;
    private long startTime;
    private long endTime;
    private String stdout;
    private String stderr;

    /**
     * 构造函数
     * @param command 命令数组
     * @param envp 环境变量数组
     * @param workDir 工作目录
     */
    public BinProcess(String[] command, String[] envp, File workDir) {
        this.command = command;
        this.envp = envp;
        this.workDir = workDir;
        this.state = ProcessState.NOT_STARTED;
    }

    /**
     * 启动进程
     * @return 是否启动成功
     */
    public boolean start() {
        try {
            startTime = System.currentTimeMillis();
            process = Runtime.getRuntime().exec(command, envp, workDir);
            state = ProcessState.RUNNING;
            BinLogger.debug("进程启动成功: " + String.join(" ", command));
            return true;
        } catch (IOException e) {
            state = ProcessState.FAILED;
            BinLogger.error("进程启动失败: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * 等待进程完成
     * @param timeout 超时时间（毫秒）
     * @return 执行结果
     */
    public ProcessResult waitFor(long timeout) {
        if (process == null) {
            return ProcessResult.failure(-1, "进程未启动", 0);
        }

        try {
            boolean completed = process.waitFor(timeout, TimeUnit.MILLISECONDS);
            endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            if (!completed) {
                process.destroyForcibly();
                state = ProcessState.TIMEOUT;
                return ProcessResult.timeout(executionTime);
            }

            // 读取输出
            stdout = readStream(process.getInputStream());
            stderr = readStream(process.getErrorStream());

            int exitCode = process.exitValue();
            if (exitCode == 0) {
                state = ProcessState.COMPLETED;
                return ProcessResult.success(stdout, executionTime);
            } else {
                state = ProcessState.FAILED;
                return new ProcessResult(exitCode, stdout, stderr, ProcessState.FAILED, executionTime);
            }
        } catch (InterruptedException e) {
            endTime = System.currentTimeMillis();
            state = ProcessState.TERMINATED;
            Thread.currentThread().interrupt();
            return ProcessResult.terminated(endTime - startTime);
        }
    }

    /**
     * 执行并等待完成（同步执行）
     * @param timeout 超时时间（毫秒）
     * @return 执行结果
     */
    public ProcessResult execute(long timeout) {
        if (!start()) {
            return ProcessResult.failure(-1, "进程启动失败", 0);
        }
        return waitFor(timeout);
    }

    /**
     * 写入数据到进程标准输入
     * @param data 要写入的数据
     * @return 是否写入成功
     */
    public boolean writeInput(String data) {
        if (process == null || !state.isRunning()) {
            return false;
        }
        try {
            OutputStream os = process.getOutputStream();
            os.write(data.getBytes(StandardCharsets.UTF_8));
            os.flush();
            return true;
        } catch (IOException e) {
            BinLogger.error("写入进程输入失败: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * 终止进程
     */
    public void destroy() {
        if (process != null) {
            process.destroyForcibly();
            state = ProcessState.TERMINATED;
            BinLogger.debug("进程已终止");
        }
    }

    /**
     * 判断进程是否存活
     * @return 是否存活
     */
    public boolean isAlive() {
        return process != null && process.isAlive();
    }

    /**
     * 获取进程状态
     * @return 进程状态
     */
    public ProcessState getState() {
        return state;
    }

    /**
     * 获取标准输出
     * @return 标准输出
     */
    public String getStdout() {
        return stdout;
    }

    /**
     * 获取标准错误
     * @return 标准错误
     */
    public String getStderr() {
        return stderr;
    }

    /**
     * 读取输入流内容
     * @param inputStream 输入流
     * @return 流内容
     */
    private String readStream(java.io.InputStream inputStream) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append(line);
            }
        } catch (IOException e) {
            BinLogger.error("读取进程输出失败: " + e.getMessage(), e);
        }
        return sb.toString();
    }
}
