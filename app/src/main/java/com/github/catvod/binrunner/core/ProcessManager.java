package com.github.catvod.binrunner.core;

import com.github.catvod.binrunner.process.BinProcess;
import com.github.catvod.binrunner.process.ProcessState;
import com.github.catvod.binrunner.util.BinLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 进程管理器
 * 负责管理所有运行中的进程
 */
public class ProcessManager {

    private static volatile ProcessManager instance;
    private final Map<String, BinProcess> processes;
    private int processCounter = 0;

    private ProcessManager() {
        processes = new ConcurrentHashMap<>();
    }

    /**
     * 获取单例实例
     * @return 进程管理器实例
     */
    public static ProcessManager getInstance() {
        if (instance == null) {
            synchronized (ProcessManager.class) {
                if (instance == null) {
                    instance = new ProcessManager();
                }
            }
        }
        return instance;
    }

    /**
     * 注册进程
     * @param process 进程对象
     * @return 进程ID
     */
    public synchronized String register(BinProcess process) {
        String processId = "proc_" + (++processCounter);
        processes.put(processId, process);
        BinLogger.debug("进程已注册: " + processId);
        return processId;
    }

    /**
     * 根据ID获取进程
     * @param processId 进程ID
     * @return 进程对象，不存在时返回 null
     */
    public BinProcess getProcess(String processId) {
        return processes.get(processId);
    }

    /**
     * 终止指定进程
     * @param processId 进程ID
     * @return 是否终止成功
     */
    public boolean destroy(String processId) {
        BinProcess process = processes.remove(processId);
        if (process != null) {
            process.destroy();
            BinLogger.debug("进程已终止: " + processId);
            return true;
        }
        return false;
    }

    /**
     * 终止所有进程
     */
    public void destroyAll() {
        for (Map.Entry<String, BinProcess> entry : processes.entrySet()) {
            entry.getValue().destroy();
            BinLogger.debug("进程已终止: " + entry.getKey());
        }
        processes.clear();
        BinLogger.info("所有进程已终止");
    }

    /**
     * 获取所有运行中的进程ID
     * @return 进程ID列表
     */
    public List<String> getRunningProcessIds() {
        List<String> running = new ArrayList<>();
        for (Map.Entry<String, BinProcess> entry : processes.entrySet()) {
            if (entry.getValue().isAlive()) {
                running.add(entry.getKey());
            }
        }
        return running;
    }

    /**
     * 获取进程数量
     * @return 进程数量
     */
    public int getProcessCount() {
        return processes.size();
    }

    /**
     * 获取运行中的进程数量
     * @return 运行中的进程数量
     */
    public int getRunningProcessCount() {
        int count = 0;
        for (BinProcess process : processes.values()) {
            if (process.isAlive()) {
                count++;
            }
        }
        return count;
    }

    /**
     * 清理已结束的进程
     */
    public void cleanupFinished() {
        List<String> finished = new ArrayList<>();
        for (Map.Entry<String, BinProcess> entry : processes.entrySet()) {
            if (!entry.getValue().isAlive()) {
                finished.add(entry.getKey());
            }
        }
        for (String id : finished) {
            processes.remove(id);
            BinLogger.debug("已清理结束的进程: " + id);
        }
    }

    /**
     * 获取所有进程状态信息
     * @return 进程状态信息映射
     */
    public Map<String, ProcessState> getAllProcessStates() {
        Map<String, ProcessState> states = new HashMap<>();
        for (Map.Entry<String, BinProcess> entry : processes.entrySet()) {
            states.put(entry.getKey(), entry.getValue().getState());
        }
        return states;
    }
}
