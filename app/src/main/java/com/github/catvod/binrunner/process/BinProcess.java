package com.github.catvod.binrunner.process;

import com.github.catvod.binrunner.config.AutoStartConfig;
import com.github.catvod.binrunner.util.FileHelper;

/**
 * Process encapsulation for BinRunner.
 * Wraps a native Process with additional metadata and control.
 */
public class BinProcess {

    private final int id;
    private final String command;
    private final String name;
    private final boolean background;
    private final boolean autoRestart;
    private final int maxRestarts;
    private final long startTime;

    private Process process;
    private ProcessState state;
    private int restartCount;
    private String lastOutput;
    private String lastError;

    public BinProcess(int id, String command, String name, boolean background) {
        this(id, command, name, background, false, 0);
    }

    public BinProcess(int id, String command, String name, boolean background, boolean autoRestart, int maxRestarts) {
        this.id = id;
        this.command = command;
        this.name = name;
        this.background = background;
        this.autoRestart = autoRestart;
        this.maxRestarts = maxRestarts;
        this.startTime = System.currentTimeMillis();
        this.state = ProcessState.PENDING;
        this.restartCount = 0;
        this.lastOutput = "";
        this.lastError = "";
    }

    /**
     * Create a BinProcess from AutoStartConfig.
     *
     * @param id process id
     * @param config auto-start configuration
     * @return new BinProcess instance
     */
    public static BinProcess fromAutoStart(int id, AutoStartConfig config) {
        return new BinProcess(
            id,
            config.getCmd(),
            config.getName(),
            config.isBackground(),
            config.isAutoRestart(),
            config.getMaxRestarts()
        );
    }

    public int getId() {
        return id;
    }

    /**
     * Get the real system PID of the process.
     *
     * @return system PID or -1 if not available
     */
    public long getPid() {
        if (process != null) {
            try {
                return process.pid();
            } catch (Exception e) {
                return -1;
            }
        }
        return -1;
    }

    public String getCommand() {
        return command;
    }

    public String getName() {
        return name != null && !name.isEmpty() ? name : command;
    }

    public boolean isBackground() {
        return background;
    }

    public boolean isAutoRestart() {
        return autoRestart;
    }

    public int getMaxRestarts() {
        return maxRestarts;
    }

    public long getStartTime() {
        return startTime;
    }

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
        this.state = ProcessState.RUNNING;
    }

    public ProcessState getState() {
        return state;
    }

    public void setState(ProcessState state) {
        this.state = state;
    }

    public int getRestartCount() {
        return restartCount;
    }

    public void incrementRestartCount() {
        this.restartCount++;
    }

    /**
     * Check if process can be restarted.
     *
     * @return true if restart is allowed
     */
    public boolean canRestart() {
        return autoRestart && restartCount < maxRestarts;
    }

    /**
     * Check if process is still alive.
     *
     * @return true if process is running
     */
    public boolean isAlive() {
        return process != null && process.isAlive();
    }

    /**
     * Terminate the process.
     */
    public void destroy() {
        if (process != null) {
            process.destroy();
            state = ProcessState.STOPPED;
        }
    }

    /**
     * Force terminate the process.
     */
    public void destroyForcibly() {
        if (process != null) {
            process.destroyForcibly();
            state = ProcessState.STOPPED;
        }
    }

    /**
     * Wait for process to complete.
     *
     * @return exit code
     * @throws InterruptedException if interrupted
     */
    public int waitFor() throws InterruptedException {
        if (process != null) {
            return process.waitFor();
        }
        return -1;
    }

    /**
     * Read output from process.
     *
     * @return output string
     */
    public String readOutput() {
        if (process == null) return "";
        return FileHelper.readStream(process.getInputStream());
    }

    /**
     * Read error from process.
     *
     * @return error string
     */
    public String readError() {
        if (process == null) return "";
        return FileHelper.readStream(process.getErrorStream());
    }

    public String getLastOutput() {
        return lastOutput;
    }

    public void setLastOutput(String lastOutput) {
        this.lastOutput = lastOutput;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    /**
     * Get uptime in milliseconds.
     *
     * @return uptime in ms
     */
    public long getUptime() {
        return System.currentTimeMillis() - startTime;
    }

    /**
     * Get formatted uptime string.
     *
     * @return formatted uptime
     */
    public String getFormattedUptime() {
        long uptime = getUptime();
        long seconds = uptime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes % 60, seconds % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
        }
    }

    @Override
    public String toString() {
        return String.format("[%d] %s (%s) - %s - uptime: %s",
            id, getName(), state.name(), isAlive() ? "alive" : "dead", getFormattedUptime());
    }
}
