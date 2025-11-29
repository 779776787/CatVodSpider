package com.github.catvod.spider;

import android.content.Context;

import com.github.catvod.bean.Class;
import com.github.catvod.bean.Result;
import com.github.catvod.bean.Vod;
import com.github.catvod.binrunner.config.AutoStartConfig;
import com.github.catvod.binrunner.config.EnvsConfig;
import com.github.catvod.binrunner.core.BinExecutor;
import com.github.catvod.binrunner.core.EnvManager;
import com.github.catvod.binrunner.core.ProcessManager;
import com.github.catvod.binrunner.process.BinProcess;
import com.github.catvod.binrunner.process.ProcessResult;
import com.github.catvod.binrunner.terminal.Terminal;
import com.github.catvod.binrunner.util.BinLogger;
import com.github.catvod.binrunner.util.FileHelper;
import com.github.catvod.crawler.Spider;
import com.github.catvod.utils.Path;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BinRunner Spider - Binary execution environment for Android TV.
 * 
 * This spider enables execution of arbitrary binary files (PHP, Python, Node.js, etc.)
 * with interactive terminal, background process management, and auto-start capabilities.
 * 
 * Directory structure:
 * - User directory: /storage/emulated/0/TV/envs/
 * - Private directory: /data/data/{package}/files/binrunner/
 */
public class BinRunner extends Spider {

    private static final String ENVS_DIR = "envs";
    private static final String CONFIG_FILE = "envs.json";
    private static final String DEFAULT_LOG_PATH = "logs";

    private Context context;
    private EnvsConfig config;
    private EnvManager envManager;
    private ProcessManager processManager;
    private BinExecutor executor;
    private BinLogger logger;
    private Terminal terminal;
    private boolean initialized;

    @Override
    public void init(Context context, String extend) throws Exception {
        this.context = context;
        this.initialized = false;

        // Load configuration
        File envsDir = new File(Path.tv(), ENVS_DIR);
        File configFile = new File(envsDir, CONFIG_FILE);
        
        if (configFile.exists()) {
            String configJson = FileHelper.readString(configFile);
            config = EnvsConfig.fromJson(configJson);
        } else {
            config = new EnvsConfig();
        }

        // Initialize logger
        String logPath = config.getSettings().getLog().getPath();
        if (logPath == null || logPath.isEmpty()) {
            logPath = new File(envsDir, DEFAULT_LOG_PATH).getAbsolutePath();
        }
        config.getSettings().getLog().setPath(logPath);
        logger = new BinLogger(config.getSettings().getLog());
        logger.init();

        // Initialize environment manager
        File privateDir = context.getFilesDir();
        envManager = new EnvManager(envsDir, privateDir, config);
        if (!envManager.init()) {
            logger.error("Failed to initialize environment manager", null);
        }

        // Initialize process manager
        processManager = new ProcessManager(config.getSettings().getMaxProcesses());

        // Initialize executor
        executor = new BinExecutor(envManager, processManager, config, logger);

        // Initialize terminal
        terminal = new Terminal(executor, processManager, envManager);

        // Execute auto-start commands
        executeAutoStart();

        initialized = true;
        logger.info("BinRunner initialized successfully");
    }

    /**
     * Execute auto-start commands from configuration.
     */
    private void executeAutoStart() {
        List<AutoStartConfig> autoStarts = config.getSettings().getAutoStart();
        for (AutoStartConfig autoStart : autoStarts) {
            if (autoStart.getCmd() == null || autoStart.getCmd().isEmpty()) {
                continue;
            }

            BinProcess process = executor.executeBackground(
                autoStart.getCmd(),
                autoStart.getName(),
                autoStart.isAutoRestart(),
                autoStart.getMaxRestarts()
            );

            if (process != null) {
                logger.info("Auto-started: " + autoStart.getName());
            } else {
                logger.error("Failed to auto-start: " + autoStart.getName(), null);
            }
        }
    }

    @Override
    public String homeContent(boolean filter) throws Exception {
        List<Class> classes = new ArrayList<>();
        classes.add(new Class("terminal", "终端", "1"));
        classes.add(new Class("processes", "进程管理", "1"));
        classes.add(new Class("commands", "可用命令", "1"));
        classes.add(new Class("logs", "日志", "1"));
        
        return Result.string(classes);
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) throws Exception {
        List<Vod> items = new ArrayList<>();
        
        switch (tid) {
            case "terminal":
                items.add(createVod("terminal_main", "交互式终端", "进入终端执行命令"));
                break;
            case "processes":
                List<BinProcess> processes = processManager.getAllProcesses();
                if (processes.isEmpty()) {
                    items.add(createVod("no_process", "无进程", "当前没有运行中的进程"));
                } else {
                    for (BinProcess p : processes) {
                        items.add(createVod(
                            "process_" + p.getId(),
                            "[" + p.getId() + "] " + p.getName(),
                            p.getState().name() + " - " + p.getFormattedUptime()
                        ));
                    }
                }
                break;
            case "commands":
                Map<String, EnvManager.CommandInfo> commands = envManager.getAllCommands();
                if (commands.isEmpty()) {
                    items.add(createVod("no_commands", "无可用命令", "请在 envs 目录添加二进制文件"));
                } else {
                    for (EnvManager.CommandInfo info : commands.values()) {
                        items.add(createVod(
                            "cmd_" + info.name,
                            info.name,
                            info.envType + " - " + info.binPath
                        ));
                    }
                }
                break;
            case "logs":
                items.add(createVod("logs_view", "查看日志", "查看执行日志"));
                break;
        }
        
        return Result.get().vod(items).page().string();
    }

    @Override
    public String detailContent(List<String> ids) throws Exception {
        String id = ids.get(0);
        Vod vod = new Vod();
        
        if (id.startsWith("process_")) {
            int processId = Integer.parseInt(id.substring(8));
            BinProcess process = processManager.getProcess(processId);
            if (process != null) {
                vod.setVodId(id);
                vod.setVodName("[" + process.getId() + "] " + process.getName());
                vod.setVodContent(buildProcessDetail(process));
                vod.setVodPlayFrom("操作");
                vod.setVodPlayUrl("终止进程$kill_" + processId);
            }
        } else if (id.startsWith("cmd_")) {
            String cmdName = id.substring(4);
            EnvManager.CommandInfo info = envManager.getCommand(cmdName);
            if (info != null) {
                vod.setVodId(id);
                vod.setVodName(info.name);
                vod.setVodContent(buildCommandDetail(info));
                vod.setVodPlayFrom("执行");
                vod.setVodPlayUrl("版本$exec_" + cmdName + "_--version");
            }
        } else if (id.equals("terminal_main")) {
            vod.setVodId(id);
            vod.setVodName("交互式终端");
            vod.setVodContent(terminal.getWelcome() + "\n\n" + getTerminalHelp());
        }
        
        return Result.string(vod);
    }

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) throws Exception {
        String result = "";
        
        if (id.startsWith("kill_")) {
            int processId = Integer.parseInt(id.substring(5));
            if (processManager.kill(processId)) {
                result = "进程 " + processId + " 已终止";
            } else {
                result = "终止进程失败";
            }
        } else if (id.startsWith("exec_")) {
            String[] parts = id.substring(5).split("_", 2);
            if (parts.length >= 1) {
                String cmd = parts[0];
                String args = parts.length > 1 ? parts[1].replace("_", " ") : "";
                ProcessResult processResult = executor.execute(cmd + " " + args);
                result = processResult.getCombinedOutput();
            }
        }
        
        return Result.get().url("").msg(result).string();
    }

    @Override
    public String action(String action) throws Exception {
        if (action == null || action.isEmpty()) {
            return Result.error("Empty action");
        }
        
        // Terminal command execution
        if (action.startsWith("cmd:")) {
            String command = action.substring(4);
            String output = terminal.processInput(command);
            return Result.get().msg(output).string();
        }
        
        // Process control
        if (action.startsWith("kill:")) {
            try {
                int pid = Integer.parseInt(action.substring(5));
                boolean killed = processManager.kill(pid);
                return Result.get().msg(killed ? "Process killed" : "Process not found").string();
            } catch (NumberFormatException e) {
                return Result.error("Invalid process ID");
            }
        }
        
        // Reload environment
        if (action.equals("reload")) {
            envManager.reload();
            return Result.get().msg("Environment reloaded").string();
        }
        
        return Result.error("Unknown action: " + action);
    }

    @Override
    public void destroy() {
        // Shutdown all processes
        if (processManager != null) {
            processManager.shutdown();
        }
        
        // Close logger
        if (logger != null) {
            logger.close();
        }
        
        initialized = false;
    }

    /**
     * Create a simple Vod item.
     */
    private Vod createVod(String id, String name, String remarks) {
        Vod vod = new Vod();
        vod.setVodId(id);
        vod.setVodName(name);
        vod.setVodRemarks(remarks);
        return vod;
    }

    /**
     * Build process detail content.
     */
    private String buildProcessDetail(BinProcess process) {
        StringBuilder sb = new StringBuilder();
        sb.append("进程ID: ").append(process.getId()).append("\n");
        sb.append("系统PID: ").append(process.getPid()).append("\n");
        sb.append("状态: ").append(process.getState().name()).append("\n");
        sb.append("命令: ").append(process.getCommand()).append("\n");
        sb.append("运行时间: ").append(process.getFormattedUptime()).append("\n");
        sb.append("后台运行: ").append(process.isBackground() ? "是" : "否").append("\n");
        sb.append("自动重启: ").append(process.isAutoRestart() ? "是" : "否").append("\n");
        if (process.isAutoRestart()) {
            sb.append("重启次数: ").append(process.getRestartCount())
              .append("/").append(process.getMaxRestarts()).append("\n");
        }
        return sb.toString();
    }

    /**
     * Build command detail content.
     */
    private String buildCommandDetail(EnvManager.CommandInfo info) {
        StringBuilder sb = new StringBuilder();
        sb.append("命令名称: ").append(info.name).append("\n");
        sb.append("环境类型: ").append(info.envType).append("\n");
        sb.append("可执行路径: ").append(info.binPath).append("\n");
        if (info.libPath != null) {
            sb.append("库路径: ").append(info.libPath).append("\n");
        }
        return sb.toString();
    }

    /**
     * Get terminal help text.
     */
    private String getTerminalHelp() {
        return "使用 action 接口发送命令：\n" +
               "- cmd:命令内容 - 执行命令\n" +
               "- kill:进程ID - 终止进程\n" +
               "- reload - 重新加载环境\n\n" +
               "支持后台运行：命令 &";
    }
}
