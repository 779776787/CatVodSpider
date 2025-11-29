package com.github.catvod.binrunner.core;

import com.github.catvod.binrunner.config.AutoStartConfig;
import com.github.catvod.binrunner.process.BinProcess;
import com.github.catvod.binrunner.process.ProcessState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Process manager for BinRunner.
 * Handles process lifecycle, concurrency control, and auto-restart.
 */
public class ProcessManager {

    private final int maxProcesses;
    private final Map<Integer, BinProcess> processes;
    private final AtomicInteger idGenerator;

    public ProcessManager(int maxProcesses) {
        this.maxProcesses = maxProcesses;
        this.processes = new ConcurrentHashMap<>();
        this.idGenerator = new AtomicInteger(1);
    }

    /**
     * Generate a new process ID.
     *
     * @return new unique ID
     */
    public int generateId() {
        return idGenerator.getAndIncrement();
    }

    /**
     * Check if we can start a new process.
     *
     * @return true if within limit
     */
    public boolean canStartProcess() {
        if (maxProcesses <= 0) {
            return true; // No limit
        }
        return getRunningCount() < maxProcesses;
    }

    /**
     * Register a new process.
     *
     * @param process the process to register
     * @return true if registered
     */
    public boolean register(BinProcess process) {
        if (!canStartProcess()) {
            return false;
        }
        processes.put(process.getId(), process);
        return true;
    }

    /**
     * Get process by ID.
     *
     * @param id process ID
     * @return BinProcess or null
     */
    public BinProcess getProcess(int id) {
        return processes.get(id);
    }

    /**
     * Get process by PID.
     *
     * @param pid system PID
     * @return BinProcess or null
     */
    public BinProcess getProcessByPid(long pid) {
        for (BinProcess p : processes.values()) {
            if (p.getPid() == pid) {
                return p;
            }
        }
        return null;
    }

    /**
     * Remove a process from management.
     *
     * @param id process ID
     * @return removed process or null
     */
    public BinProcess remove(int id) {
        return processes.remove(id);
    }

    /**
     * Kill a process by ID.
     *
     * @param id process ID
     * @return true if killed
     */
    public boolean kill(int id) {
        BinProcess process = processes.get(id);
        if (process != null) {
            process.destroy();
            process.setState(ProcessState.STOPPED);
            return true;
        }
        return false;
    }

    /**
     * Force kill a process by ID.
     *
     * @param id process ID
     * @return true if killed
     */
    public boolean forceKill(int id) {
        BinProcess process = processes.get(id);
        if (process != null) {
            process.destroyForcibly();
            process.setState(ProcessState.STOPPED);
            return true;
        }
        return false;
    }

    /**
     * Get all running processes.
     *
     * @return list of running processes
     */
    public List<BinProcess> getRunningProcesses() {
        List<BinProcess> running = new ArrayList<>();
        for (BinProcess p : processes.values()) {
            if (p.isAlive()) {
                running.add(p);
            }
        }
        return running;
    }

    /**
     * Get all background processes.
     *
     * @return list of background processes
     */
    public List<BinProcess> getBackgroundProcesses() {
        List<BinProcess> background = new ArrayList<>();
        for (BinProcess p : processes.values()) {
            if (p.isBackground() && p.isAlive()) {
                background.add(p);
            }
        }
        return background;
    }

    /**
     * Get all processes (running and completed).
     *
     * @return list of all processes
     */
    public List<BinProcess> getAllProcesses() {
        return new ArrayList<>(processes.values());
    }

    /**
     * Get count of running processes.
     *
     * @return running process count
     */
    public int getRunningCount() {
        int count = 0;
        for (BinProcess p : processes.values()) {
            if (p.isAlive()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Get total process count.
     *
     * @return total process count
     */
    public int getTotalCount() {
        return processes.size();
    }

    /**
     * Clean up finished processes.
     */
    public void cleanup() {
        List<Integer> toRemove = new ArrayList<>();
        for (Map.Entry<Integer, BinProcess> entry : processes.entrySet()) {
            BinProcess p = entry.getValue();
            if (!p.isAlive() && !p.canRestart()) {
                toRemove.add(entry.getKey());
            }
        }
        for (Integer id : toRemove) {
            processes.remove(id);
        }
    }

    /**
     * Get processes that need restart.
     *
     * @return list of processes needing restart
     */
    public List<BinProcess> getProcessesNeedingRestart() {
        List<BinProcess> needRestart = new ArrayList<>();
        for (BinProcess p : processes.values()) {
            if (!p.isAlive() && p.canRestart() && 
                (p.getState() == ProcessState.ERROR || p.getState() == ProcessState.FINISHED)) {
                needRestart.add(p);
            }
        }
        return needRestart;
    }

    /**
     * Create a process for auto-start configuration.
     *
     * @param config auto-start configuration
     * @return new BinProcess
     */
    public BinProcess createFromAutoStart(AutoStartConfig config) {
        return BinProcess.fromAutoStart(generateId(), config);
    }

    /**
     * Shutdown all processes.
     */
    public void shutdown() {
        for (BinProcess p : processes.values()) {
            if (p.isAlive()) {
                p.destroy();
            }
        }
        processes.clear();
    }

    /**
     * Get process status summary.
     *
     * @return formatted status string
     */
    public String getStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("Processes: ").append(getRunningCount()).append(" running / ")
          .append(getTotalCount()).append(" total");
        if (maxProcesses > 0) {
            sb.append(" (max: ").append(maxProcesses).append(")");
        }
        return sb.toString();
    }
}
