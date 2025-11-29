package com.github.catvod.spider.binrunner.process;

/**
 * 进程执行结果类
 * 封装进程执行后的输出和状态
 */
public class ProcessResult {

    private final int exitCode;
    private final String stdout;
    private final String stderr;
    private final ProcessState state;
    private final long executionTime;

    private ProcessResult(Builder builder) {
        this.exitCode = builder.exitCode;
        this.stdout = builder.stdout;
        this.stderr = builder.stderr;
        this.state = builder.state;
        this.executionTime = builder.executionTime;
    }

    /**
     * 创建成功结果
     */
    public static ProcessResult success(String stdout, long executionTime) {
        return new Builder()
                .exitCode(0)
                .stdout(stdout)
                .state(ProcessState.COMPLETED)
                .executionTime(executionTime)
                .build();
    }

    /**
     * 创建失败结果
     */
    public static ProcessResult failure(String stderr, int exitCode) {
        return new Builder()
                .exitCode(exitCode)
                .stderr(stderr)
                .state(ProcessState.FAILED)
                .build();
    }

    /**
     * 创建超时结果
     */
    public static ProcessResult timeout(String message) {
        return new Builder()
                .exitCode(-1)
                .stderr(message)
                .state(ProcessState.TIMEOUT)
                .build();
    }

    /**
     * 创建取消结果
     */
    public static ProcessResult cancelled() {
        return new Builder()
                .exitCode(-2)
                .stderr("进程已取消")
                .state(ProcessState.CANCELLED)
                .build();
    }

    /**
     * 获取退出码
     */
    public int getExitCode() {
        return exitCode;
    }

    /**
     * 获取标准输出
     */
    public String getStdout() {
        return stdout != null ? stdout : "";
    }

    /**
     * 获取标准错误输出
     */
    public String getStderr() {
        return stderr != null ? stderr : "";
    }

    /**
     * 获取进程状态
     */
    public ProcessState getState() {
        return state;
    }

    /**
     * 获取执行时间（毫秒）
     */
    public long getExecutionTime() {
        return executionTime;
    }

    /**
     * 是否执行成功
     */
    public boolean isSuccess() {
        return state == ProcessState.COMPLETED && exitCode == 0;
    }

    /**
     * 获取输出内容（优先返回 stdout，如果为空则返回 stderr）
     */
    public String getOutput() {
        if (stdout != null && !stdout.isEmpty()) {
            return stdout;
        }
        return stderr != null ? stderr : "";
    }

    /**
     * 构建器类
     */
    public static class Builder {
        private int exitCode = -1;
        private String stdout;
        private String stderr;
        private ProcessState state = ProcessState.PENDING;
        private long executionTime = 0;

        public Builder exitCode(int exitCode) {
            this.exitCode = exitCode;
            return this;
        }

        public Builder stdout(String stdout) {
            this.stdout = stdout;
            return this;
        }

        public Builder stderr(String stderr) {
            this.stderr = stderr;
            return this;
        }

        public Builder state(ProcessState state) {
            this.state = state;
            return this;
        }

        public Builder executionTime(long executionTime) {
            this.executionTime = executionTime;
            return this;
        }

        public ProcessResult build() {
            return new ProcessResult(this);
        }
    }
}
