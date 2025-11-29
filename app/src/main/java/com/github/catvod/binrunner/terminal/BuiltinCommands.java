package com.github.catvod.binrunner.terminal;

import com.github.catvod.binrunner.core.EnvManager;
import com.github.catvod.binrunner.core.ProcessManager;
import com.github.catvod.binrunner.process.ProcessState;

import java.util.Map;

/**
 * 内置命令处理器
 * 处理终端的内置命令
 */
public class BuiltinCommands {

    private final EnvManager envManager;
    private final ProcessManager processManager;

    /**
     * 构造函数
     * @param envManager 环境管理器
     * @param processManager 进程管理器
     */
    public BuiltinCommands(EnvManager envManager, ProcessManager processManager) {
        this.envManager = envManager;
        this.processManager = processManager;
    }

    /**
     * 执行内置命令
     * @param command 命令名称
     * @param args 参数列表
     * @return 命令输出
     */
    public String execute(String command, java.util.List<String> args) {
        if (command == null || command.isEmpty()) {
            return "";
        }

        switch (command.toLowerCase()) {
            case "help":
                return showHelp();
            case "env":
                return showEnv(args);
            case "pwd":
                return showPwd();
            case "echo":
                return echo(args);
            case "clear":
                return clear();
            case "history":
                return showHistory();
            case "ps":
                return showProcesses();
            case "kill":
                return killProcess(args);
            default:
                return "未知命令: " + command;
        }
    }

    /**
     * 显示帮助信息
     * @return 帮助信息
     */
    private String showHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append("========== BinRunner 终端帮助 ==========\n\n");
        sb.append("内置命令:\n");
        sb.append("  help     - 显示帮助信息\n");
        sb.append("  env      - 显示环境变量\n");
        sb.append("  pwd      - 显示当前工作目录\n");
        sb.append("  echo     - 输出文本\n");
        sb.append("  clear    - 清屏\n");
        sb.append("  ps       - 显示运行中的进程\n");
        sb.append("  kill     - 终止进程\n");
        sb.append("\n");
        sb.append("脚本执行:\n");
        sb.append("  php80 script.php [args]    - 执行 PHP 脚本\n");
        sb.append("  python3 script.py [args]   - 执行 Python 脚本\n");
        sb.append("  node script.js [args]      - 执行 JavaScript 脚本\n");
        sb.append("  lua script.lua [args]      - 执行 Lua 脚本\n");
        sb.append("\n");
        sb.append("特殊语法:\n");
        sb.append("  command &    - 后台运行\n");
        sb.append("  cmd1 | cmd2  - 管道\n");
        sb.append("\n");
        sb.append("=========================================\n");
        return sb.toString();
    }

    /**
     * 显示环境变量
     * @param args 参数（可选的变量名）
     * @return 环境变量信息
     */
    private String showEnv(java.util.List<String> args) {
        if (envManager == null) {
            return "环境管理器未初始化";
        }

        StringBuilder sb = new StringBuilder();
        Map<String, String> envMap = envManager.getEnvMap();

        if (args.isEmpty()) {
            // 显示所有环境变量
            sb.append("========== 环境变量 ==========\n");
            for (Map.Entry<String, String> entry : envMap.entrySet()) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
            }
            sb.append("\n========== 解释器路径 ==========\n");
            sb.append("PHP: ").append(nvl(envManager.getPhpPath())).append("\n");
            sb.append("Python: ").append(nvl(envManager.getPythonPath())).append("\n");
            sb.append("Node.js: ").append(nvl(envManager.getNodePath())).append("\n");
            sb.append("Lua: ").append(nvl(envManager.getLuaPath())).append("\n");
        } else {
            // 显示指定的环境变量
            String varName = args.get(0);
            String value = envMap.get(varName);
            if (value != null) {
                sb.append(varName).append("=").append(value);
            } else {
                sb.append("环境变量 ").append(varName).append(" 未设置");
            }
        }

        return sb.toString();
    }

    /**
     * 显示当前工作目录
     * @return 工作目录
     */
    private String showPwd() {
        if (envManager == null) {
            return "环境管理器未初始化";
        }
        return envManager.getWorkDir();
    }

    /**
     * 输出文本
     * @param args 要输出的文本
     * @return 输出结果
     */
    private String echo(java.util.List<String> args) {
        return String.join(" ", args);
    }

    /**
     * 清屏
     * @return 清屏指令
     */
    private String clear() {
        return "\033[2J\033[H"; // ANSI 清屏转义序列
    }

    /**
     * 显示命令历史
     * @return 历史记录
     */
    private String showHistory() {
        return "历史记录功能暂未实现";
    }

    /**
     * 显示运行中的进程
     * @return 进程列表
     */
    private String showProcesses() {
        if (processManager == null) {
            return "进程管理器未初始化";
        }

        Map<String, ProcessState> states = processManager.getAllProcessStates();
        if (states.isEmpty()) {
            return "没有运行中的进程";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("========== 进程列表 ==========\n");
        sb.append(String.format("%-12s %-10s\n", "进程ID", "状态"));
        sb.append("------------------------------\n");
        for (Map.Entry<String, ProcessState> entry : states.entrySet()) {
            sb.append(String.format("%-12s %-10s\n", entry.getKey(), entry.getValue().getDescription()));
        }
        sb.append("------------------------------\n");
        sb.append("总计: ").append(states.size()).append(" 个进程\n");

        return sb.toString();
    }

    /**
     * 终止进程
     * @param args 进程ID列表
     * @return 执行结果
     */
    private String killProcess(java.util.List<String> args) {
        if (processManager == null) {
            return "进程管理器未初始化";
        }

        if (args.isEmpty()) {
            return "用法: kill <进程ID>";
        }

        StringBuilder sb = new StringBuilder();
        for (String processId : args) {
            if (processManager.destroy(processId)) {
                sb.append("进程 ").append(processId).append(" 已终止\n");
            } else {
                sb.append("进程 ").append(processId).append(" 不存在或已终止\n");
            }
        }

        return sb.toString().trim();
    }

    /**
     * 空值处理
     * @param value 值
     * @return 非空值或 "未设置"
     */
    private String nvl(String value) {
        return value != null && !value.isEmpty() ? value : "未设置";
    }
}
