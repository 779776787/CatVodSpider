package com.github.catvod.spider.binrunner.terminal;

import com.github.catvod.spider.binrunner.core.BinExecutor;
import com.github.catvod.spider.binrunner.core.EnvManager;
import com.github.catvod.spider.binrunner.process.ProcessResult;
import com.github.catvod.spider.binrunner.util.BinLogger;
import com.github.catvod.spider.binrunner.util.EnvBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 交互式终端
 * 提供命令行交互界面
 */
public class Terminal {

    private final EnvManager envManager;
    private final BinExecutor executor;
    private final Map<String, String> sessionEnv;
    private final List<String> history;
    private boolean running;
    private String currentDir;
    private TerminalCallback callback;

    /**
     * 终端回调接口
     */
    public interface TerminalCallback {
        void onOutput(String output);
        void onError(String error);
        void onExit();
    }

    /**
     * 创建终端
     */
    public Terminal(EnvManager envManager) {
        this.envManager = envManager;
        this.executor = new BinExecutor(envManager);
        this.sessionEnv = new HashMap<>();
        this.history = new ArrayList<>();
        this.running = false;
        this.currentDir = envManager.getPrivateDir().getAbsolutePath();
        
        // 初始化会话环境变量
        sessionEnv.putAll(System.getenv());
        sessionEnv.put("HOME", envManager.getPrivateDir().getAbsolutePath());
        sessionEnv.put("PWD", currentDir);
    }

    /**
     * 设置回调
     */
    public void setCallback(TerminalCallback callback) {
        this.callback = callback;
    }

    /**
     * 启动终端
     */
    public void start() {
        running = true;
        BinLogger.i("终端已启动");
        output(getWelcomeMessage());
    }

    /**
     * 处理输入
     * @param input 用户输入
     * @return 输出结果
     */
    public String processInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }

        String line = input.trim();
        
        // 添加到历史记录
        history.add(line);

        // 展开环境变量
        line = CommandParser.expandVariables(line, sessionEnv);

        // 检查管道命令
        List<String> pipeCommands = CommandParser.splitPipe(line);
        if (pipeCommands.size() > 1) {
            return processPipeline(pipeCommands);
        }

        // 解析命令
        CommandParser.ParsedCommand parsed = CommandParser.parse(line);
        if (parsed == null) {
            return "";
        }

        // 执行命令
        return executeCommand(parsed);
    }

    /**
     * 执行单个命令
     */
    private String executeCommand(CommandParser.ParsedCommand parsed) {
        String command = parsed.getCommand();
        String[] args = parsed.getArgs();

        // 检查内置命令
        if (BuiltinCommands.isBuiltin(command)) {
            return BuiltinCommands.execute(command, args, this);
        }

        // 特殊命令处理
        if (command.equals("cd")) {
            return changeDirectory(args);
        } else if (command.equals("export")) {
            return exportVariable(args);
        }

        // 外部命令执行
        return executeExternal(parsed);
    }

    /**
     * 执行外部命令
     */
    private String executeExternal(CommandParser.ParsedCommand parsed) {
        try {
            // 构建环境变量
            EnvBuilder envBuilder = new EnvBuilder();
            envBuilder.merge(sessionEnv);
            
            // 添加二进制路径到 PATH
            String binPath = envManager.getEnvsDir().getAbsolutePath();
            envBuilder.addPath(binPath);

            // 执行命令
            ProcessResult result = executor.execute(parsed.getFullCommand(), envBuilder.getEnv(), 
                    envManager.getConfig().getSettings().getTimeout());

            if (result.isSuccess()) {
                return result.getStdout();
            } else {
                String output = result.getStdout();
                String error = result.getStderr();
                if (output != null && !output.isEmpty()) {
                    return output + (error != null && !error.isEmpty() ? "\n" + error : "");
                }
                return error != null ? error : "命令执行失败";
            }

        } catch (Exception e) {
            BinLogger.e("执行命令失败: " + parsed.getFullCommand(), e);
            return "错误: " + e.getMessage();
        }
    }

    /**
     * 处理管道命令
     */
    private String processPipeline(List<String> commands) {
        String input = "";
        for (String cmd : commands) {
            CommandParser.ParsedCommand parsed = CommandParser.parse(cmd);
            if (parsed == null) {
                continue;
            }
            
            // TODO: 实现真正的管道，将前一个命令的输出作为后一个命令的输入
            String result = executeCommand(parsed);
            input = result;
        }
        return input;
    }

    /**
     * 切换目录
     */
    private String changeDirectory(String[] args) {
        String newDir;
        if (args.length == 0) {
            newDir = envManager.getPrivateDir().getAbsolutePath();
        } else {
            String path = args[0];
            if (path.startsWith("/")) {
                newDir = path;
            } else if (path.equals("..")) {
                int lastSlash = currentDir.lastIndexOf('/');
                newDir = lastSlash > 0 ? currentDir.substring(0, lastSlash) : "/";
            } else {
                newDir = currentDir + "/" + path;
            }
        }

        java.io.File dir = new java.io.File(newDir);
        if (dir.exists() && dir.isDirectory()) {
            currentDir = dir.getAbsolutePath();
            sessionEnv.put("PWD", currentDir);
            return "";
        } else {
            return "目录不存在: " + newDir;
        }
    }

    /**
     * 导出环境变量
     */
    private String exportVariable(String[] args) {
        if (args.length == 0) {
            // 显示所有导出的变量
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : sessionEnv.entrySet()) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
            }
            return sb.toString();
        }

        // 设置变量
        for (String arg : args) {
            int eqIndex = arg.indexOf('=');
            if (eqIndex > 0) {
                String key = arg.substring(0, eqIndex);
                String value = arg.substring(eqIndex + 1);
                sessionEnv.put(key, value);
            }
        }
        return "";
    }

    /**
     * 输出信息
     */
    private void output(String message) {
        if (callback != null) {
            callback.onOutput(message);
        }
    }

    /**
     * 输出错误
     */
    private void error(String message) {
        if (callback != null) {
            callback.onError(message);
        }
    }

    /**
     * 获取提示符
     */
    public String getPrompt() {
        String dir = currentDir;
        String home = envManager.getPrivateDir().getAbsolutePath();
        if (dir.startsWith(home)) {
            dir = "~" + dir.substring(home.length());
        }
        return "binrunner:" + dir + "$ ";
    }

    /**
     * 获取欢迎信息
     */
    private String getWelcomeMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("==================================\n");
        sb.append("      BinRunner 终端 v1.0\n");
        sb.append("==================================\n");
        sb.append("\n");
        sb.append("输入 'help' 获取帮助信息\n");
        sb.append("\n");
        return sb.toString();
    }

    /**
     * 获取命令历史
     */
    public List<String> getHistory() {
        return new ArrayList<>(history);
    }

    /**
     * 清空历史记录
     */
    public void clearHistory() {
        history.clear();
    }

    /**
     * 获取当前目录
     */
    public String getCurrentDir() {
        return currentDir;
    }

    /**
     * 是否正在运行
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * 关闭终端
     */
    public void close() {
        running = false;
        BinLogger.i("终端已关闭");
        if (callback != null) {
            callback.onExit();
        }
    }
}
