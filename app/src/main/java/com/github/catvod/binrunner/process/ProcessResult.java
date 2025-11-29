package com.github.catvod.binrunner.process;

/**
 * Process execution result encapsulation.
 * Contains the output, exit code, and any error information.
 */
public class ProcessResult {

    private final int exitCode;
    private final String output;
    private final String error;
    private final boolean success;
    private final long executionTime;

    private ProcessResult(int exitCode, String output, String error, boolean success, long executionTime) {
        this.exitCode = exitCode;
        this.output = output;
        this.error = error;
        this.success = success;
        this.executionTime = executionTime;
    }

    /**
     * Create a successful result.
     *
     * @param output process output
     * @param exitCode exit code
     * @param executionTime execution time in milliseconds
     * @return successful ProcessResult
     */
    public static ProcessResult success(String output, int exitCode, long executionTime) {
        return new ProcessResult(exitCode, output, "", true, executionTime);
    }

    /**
     * Create an error result.
     *
     * @param error error message
     * @param exitCode exit code
     * @param executionTime execution time in milliseconds
     * @return error ProcessResult
     */
    public static ProcessResult error(String error, int exitCode, long executionTime) {
        return new ProcessResult(exitCode, "", error, false, executionTime);
    }

    /**
     * Create a result with both output and error.
     *
     * @param output process output
     * @param error error output
     * @param exitCode exit code
     * @param executionTime execution time in milliseconds
     * @return ProcessResult with output and error
     */
    public static ProcessResult of(String output, String error, int exitCode, long executionTime) {
        return new ProcessResult(exitCode, output, error, exitCode == 0, executionTime);
    }

    /**
     * Create a timeout result.
     *
     * @param timeout timeout value in seconds
     * @return timeout ProcessResult
     */
    public static ProcessResult timeout(int timeout) {
        return new ProcessResult(-1, "", "Command timed out after " + timeout + " seconds", false, timeout * 1000L);
    }

    public int getExitCode() {
        return exitCode;
    }

    public String getOutput() {
        return output != null ? output : "";
    }

    public String getError() {
        return error != null ? error : "";
    }

    public boolean isSuccess() {
        return success;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    /**
     * Get combined output (stdout + stderr).
     *
     * @return combined output string
     */
    public String getCombinedOutput() {
        StringBuilder sb = new StringBuilder();
        if (output != null && !output.isEmpty()) {
            sb.append(output);
        }
        if (error != null && !error.isEmpty()) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(error);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "ProcessResult{" +
                "exitCode=" + exitCode +
                ", success=" + success +
                ", executionTime=" + executionTime +
                ", output='" + (output != null ? output.substring(0, Math.min(output.length(), 100)) : "") + "'" +
                "}";
    }
}
