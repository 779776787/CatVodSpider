package com.github.catvod.binrunner.process;

/**
 * 进程执行结果
 * 封装命令执行后的结果信息
 */
public class ProcessResult {

    private final int exitCode;
    private final String stdout;
    private final String stderr;
    private final ProcessState state;
    private final long executionTime;

    /**
     * 构造函数
     * @param exitCode 退出码
     * @param stdout 标准输出
     * @param stderr 标准错误
     * @param state 进程状态
     * @param executionTime 执行时间（毫秒）
     */
    public ProcessResult(int exitCode, String stdout, String stderr, ProcessState state, long executionTime) {
        this.exitCode = exitCode;
        this.stdout = stdout != null ? stdout : "";
        this.stderr = stderr != null ? stderr : "";
        this.state = state;
        this.executionTime = executionTime;
    }

    /**
     * 创建成功结果
     * @param stdout 标准输出
     * @param executionTime 执行时间
     * @return 结果对象
     */
    public static ProcessResult success(String stdout, long executionTime) {
        return new ProcessResult(0, stdout, "", ProcessState.COMPLETED, executionTime);
    }

    /**
     * 创建失败结果
     * @param exitCode 退出码
     * @param stderr 错误信息
     * @param executionTime 执行时间
     * @return 结果对象
     */
    public static ProcessResult failure(int exitCode, String stderr, long executionTime) {
        return new ProcessResult(exitCode, "", stderr, ProcessState.FAILED, executionTime);
    }

    /**
     * 创建超时结果
     * @param executionTime 执行时间
     * @return 结果对象
     */
    public static ProcessResult timeout(long executionTime) {
        return new ProcessResult(-1, "", "执行超时", ProcessState.TIMEOUT, executionTime);
    }

    /**
     * 创建终止结果
     * @param executionTime 执行时间
     * @return 结果对象
     */
    public static ProcessResult terminated(long executionTime) {
        return new ProcessResult(-1, "", "进程被终止", ProcessState.TERMINATED, executionTime);
    }

    /**
     * 获取退出码
     * @return 退出码
     */
    public int getExitCode() {
        return exitCode;
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
     * 获取进程状态
     * @return 进程状态
     */
    public ProcessState getState() {
        return state;
    }

    /**
     * 获取执行时间
     * @return 执行时间（毫秒）
     */
    public long getExecutionTime() {
        return executionTime;
    }

    /**
     * 判断是否执行成功
     * @return 是否成功
     */
    public boolean isSuccess() {
        return exitCode == 0 && state == ProcessState.COMPLETED;
    }

    /**
     * 获取所有输出（标准输出 + 标准错误）
     * @return 所有输出
     */
    public String getAllOutput() {
        StringBuilder sb = new StringBuilder();
        if (!stdout.isEmpty()) {
            sb.append(stdout);
        }
        if (!stderr.isEmpty()) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(stderr);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "ProcessResult{" +
                "exitCode=" + exitCode +
                ", state=" + state +
                ", executionTime=" + executionTime + "ms" +
                ", stdout='" + (stdout.length() > 100 ? stdout.substring(0, 100) + "..." : stdout) + '\'' +
                ", stderr='" + (stderr.length() > 100 ? stderr.substring(0, 100) + "..." : stderr) + '\'' +
                '}';
    }
}
