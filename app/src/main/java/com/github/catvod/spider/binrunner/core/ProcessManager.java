package com.github.catvod.spider.binrunner.core;

import com.github.catvod.spider.binrunner.config.AutoStartConfig;
import com.github.catvod.spider.binrunner.process.BinProcess;
import com.github.catvod.spider.binrunner.process.ProcessState;
import com.github.catvod.spider.binrunner.util.BinLogger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 进程管理器
 * 负责管理所有运行中的进程，包括：
 * 1. 进程的生命周期管理
 * 2. 后台进程管理
 * 3. 自动启动和重启
 * 4. 进程数量限制
 */
public class ProcessManager {

    private static ProcessManager instance;

    // 进程映射（进程ID -> 进程对象）
    private final Map<Integer, BinProcess> processes = new ConcurrentHashMap<>();
    // 后台进程映射
    private final Map<String, BinProcess> backgroundProcesses = new ConcurrentHashMap<>();
    // 最大进程数限制
    private int maxProcesses = 5;
    // 定时任务执行器
    private ScheduledExecutorService scheduler;
    // 是否已初始化
    private boolean initialized = false;

    private ProcessManager() {
    }

    /**
     * 获取单例实例
     */
    public static synchronized ProcessManager getInstance() {
        if (instance == null) {
            instance = new ProcessManager();
        }
        return instance;
    }

    /**
     * 初始化进程管理器
     * @param maxProcesses 最大进程数
     */
    public void init(int maxProcesses) {
        this.maxProcesses = maxProcesses;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        
        // 定期清理已完成的进程
        scheduler.scheduleAtFixedRate(this::cleanupCompletedProcesses, 30, 30, TimeUnit.SECONDS);
        
        initialized = true;
        BinLogger.i("进程管理器初始化完成，最大进程数: " + maxProcesses);
    }

    /**
     * 启动自动启动进程
     * @param configs 自动启动配置列表
     */
    public void startAutoStartProcesses(List<AutoStartConfig> configs) {
        for (AutoStartConfig config : configs) {
            startBackgroundProcess(config);
        }
    }

    /**
     * 启动后台进程
     */
    public BinProcess startBackgroundProcess(AutoStartConfig config) {
        if (backgroundProcesses.containsKey(config.getName())) {
            BinLogger.w("后台进程已存在: " + config.getName());
            return backgroundProcesses.get(config.getName());
        }

        BinProcess process = new BinProcess(config.getName(), config.getCmd(), true);
        backgroundProcesses.put(config.getName(), process);
        
        // 在新线程中启动进程
        new Thread(() -> {
            try {
                BinExecutor executor = new BinExecutor(EnvManager.getInstance());
                executor.executeBackground(process, config);
            } catch (Exception e) {
                BinLogger.e("启动后台进程失败: " + config.getName(), e);
            }
        }).start();

        BinLogger.i("已启动后台进程: " + config.getName());
        return process;
    }

    /**
     * 注册进程
     * @param process 进程对象
     * @return 是否注册成功（如果超过最大进程数会失败）
     */
    public boolean registerProcess(BinProcess process) {
        // 检查进程数限制
        if (getActiveProcessCount() >= maxProcesses) {
            BinLogger.w("已达到最大进程数限制: " + maxProcesses);
            return false;
        }

        processes.put(process.getId(), process);
        BinLogger.d("已注册进程: " + process.getId() + " - " + process.getName());
        return true;
    }

    /**
     * 注销进程
     */
    public void unregisterProcess(int processId) {
        BinProcess process = processes.remove(processId);
        if (process != null) {
            BinLogger.d("已注销进程: " + processId);
        }
    }

    /**
     * 获取进程
     */
    public BinProcess getProcess(int processId) {
        return processes.get(processId);
    }

    /**
     * 获取所有进程
     */
    public List<BinProcess> getAllProcesses() {
        return new ArrayList<>(processes.values());
    }

    /**
     * 获取所有后台进程
     */
    public List<BinProcess> getBackgroundProcesses() {
        return new ArrayList<>(backgroundProcesses.values());
    }

    /**
     * 获取活跃进程数
     */
    public int getActiveProcessCount() {
        int count = 0;
        for (BinProcess process : processes.values()) {
            if (process.isAlive()) {
                count++;
            }
        }
        return count;
    }

    /**
     * 终止进程
     * @param processId 进程ID
     * @return 是否成功
     */
    public boolean killProcess(int processId) {
        BinProcess process = processes.get(processId);
        if (process == null) {
            return false;
        }

        process.cancel();
        unregisterProcess(processId);
        BinLogger.i("已终止进程: " + processId);
        return true;
    }

    /**
     * 终止后台进程
     * @param name 进程名称
     * @return 是否成功
     */
    public boolean killBackgroundProcess(String name) {
        BinProcess process = backgroundProcesses.remove(name);
        if (process == null) {
            return false;
        }

        process.cancel();
        BinLogger.i("已终止后台进程: " + name);
        return true;
    }

    /**
     * 终止所有进程
     */
    public void killAll() {
        for (BinProcess process : processes.values()) {
            process.cancel();
        }
        processes.clear();

        for (BinProcess process : backgroundProcesses.values()) {
            process.cancel();
        }
        backgroundProcesses.clear();

        BinLogger.i("已终止所有进程");
    }

    /**
     * 清理已完成的进程
     */
    private void cleanupCompletedProcesses() {
        Iterator<Map.Entry<Integer, BinProcess>> iterator = processes.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, BinProcess> entry = iterator.next();
            BinProcess process = entry.getValue();
            if (!process.isAlive() && (process.getState() == ProcessState.COMPLETED || 
                                       process.getState() == ProcessState.FAILED ||
                                       process.getState() == ProcessState.CANCELLED)) {
                iterator.remove();
                BinLogger.d("已清理完成的进程: " + process.getId());
            }
        }
    }

    /**
     * 检查后台进程并重启
     */
    public void checkAndRestartBackgroundProcesses(List<AutoStartConfig> configs) {
        for (AutoStartConfig config : configs) {
            if (!config.isAutoRestart()) {
                continue;
            }

            BinProcess process = backgroundProcesses.get(config.getName());
            if (process == null || !process.isAlive()) {
                if (process != null && process.getRestartCount() >= config.getMaxRestarts()) {
                    BinLogger.w("后台进程已达到最大重启次数: " + config.getName());
                    continue;
                }

                // 移除旧进程
                backgroundProcesses.remove(config.getName());

                // 启动新进程
                BinProcess newProcess = new BinProcess(config.getName(), config.getCmd(), true);
                if (process != null) {
                    // 继承重启次数
                    for (int i = 0; i < process.getRestartCount(); i++) {
                        newProcess.incrementRestartCount();
                    }
                }
                newProcess.incrementRestartCount();
                
                backgroundProcesses.put(config.getName(), newProcess);
                BinLogger.i("重启后台进程: " + config.getName() + " (第 " + newProcess.getRestartCount() + " 次)");

                // 执行进程
                new Thread(() -> {
                    try {
                        BinExecutor executor = new BinExecutor(EnvManager.getInstance());
                        executor.executeBackground(newProcess, config);
                    } catch (Exception e) {
                        BinLogger.e("重启后台进程失败: " + config.getName(), e);
                    }
                }).start();
            }
        }
    }

    /**
     * 获取进程状态信息
     */
    public String getStatusInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== 进程状态 ===\n");
        sb.append("活跃进程: ").append(getActiveProcessCount()).append("/").append(maxProcesses).append("\n\n");

        sb.append("前台进程:\n");
        for (BinProcess process : processes.values()) {
            sb.append(String.format("  [%d] %s (%s) - %s\n",
                    process.getId(),
                    process.getName(),
                    process.getState().getDescription(),
                    formatRunningTime(process.getRunningTime())));
        }

        sb.append("\n后台进程:\n");
        for (BinProcess process : backgroundProcesses.values()) {
            sb.append(String.format("  [%s] %s (%s) - %s\n",
                    process.getName(),
                    process.isAlive() ? "运行中" : "已停止",
                    process.getState().getDescription(),
                    formatRunningTime(process.getRunningTime())));
        }

        return sb.toString();
    }

    /**
     * 格式化运行时间
     */
    private String formatRunningTime(long millis) {
        long seconds = millis / 1000;
        if (seconds < 60) {
            return seconds + "秒";
        }
        long minutes = seconds / 60;
        if (minutes < 60) {
            return minutes + "分" + (seconds % 60) + "秒";
        }
        long hours = minutes / 60;
        return hours + "时" + (minutes % 60) + "分";
    }

    /**
     * 关闭进程管理器
     */
    public void shutdown() {
        killAll();
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
            }
        }
        initialized = false;
        BinLogger.i("进程管理器已关闭");
    }

    /**
     * 是否已初始化
     */
    public boolean isInitialized() {
        return initialized;
    }
}
