package com.github.catvod.spider.binrunner.terminal;

import com.github.catvod.spider.binrunner.core.EnvManager;
import com.github.catvod.spider.binrunner.core.ProcessManager;
import com.github.catvod.spider.binrunner.process.BinProcess;
import com.github.catvod.spider.binrunner.util.BinLogger;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 内置命令处理器
 * 处理 ps, kill, bg, fg, env, which, clear, help, exit 等命令
 */
public class BuiltinCommands {

    private static final Set<String> BUILTIN_COMMANDS = new HashSet<>(Arrays.asList(
            "ps", "kill", "bg", "fg", "env", "which", "clear", "help", "exit", "quit"
    ));

    /**
     * 检查是否为内置命令
     */
    public static boolean isBuiltin(String command) {
        return BUILTIN_COMMANDS.contains(command.toLowerCase());
    }

    /**
     * 执行内置命令
     * @param command 命令名
     * @param args 参数
     * @param terminal 终端实例
     * @return 执行结果
     */
    public static String execute(String command, String[] args, Terminal terminal) {
        switch (command.toLowerCase()) {
            case "ps":
                return ps(args);
            case "kill":
                return kill(args);
            case "bg":
                return bg(args);
            case "fg":
                return fg(args);
            case "env":
                return env(args);
            case "which":
                return which(args);
            case "clear":
                return clear();
            case "help":
                return help(args);
            case "exit":
            case "quit":
                return exit(terminal);
            default:
                return "未知命令: " + command;
        }
    }

    /**
     * ps - 显示进程列表
     */
    private static String ps(String[] args) {
        ProcessManager pm = ProcessManager.getInstance();
        StringBuilder sb = new StringBuilder();
        
        sb.append("=== 进程列表 ===\n\n");
        
        // 前台进程
        List<BinProcess> foreground = pm.getAllProcesses();
        if (!foreground.isEmpty()) {
            sb.append("前台进程:\n");
            sb.append(String.format("%-6s %-20s %-10s %-10s\n", "PID", "名称", "状态", "运行时间"));
            sb.append("-".repeat(50)).append("\n");
            for (BinProcess p : foreground) {
                sb.append(String.format("%-6d %-20s %-10s %-10s\n",
                        p.getId(),
                        truncate(p.getName(), 20),
                        p.getState().getDescription(),
                        formatTime(p.getRunningTime())));
            }
            sb.append("\n");
        }
        
        // 后台进程
        List<BinProcess> background = pm.getBackgroundProcesses();
        if (!background.isEmpty()) {
            sb.append("后台进程:\n");
            sb.append(String.format("%-20s %-10s %-10s\n", "名称", "状态", "运行时间"));
            sb.append("-".repeat(50)).append("\n");
            for (BinProcess p : background) {
                sb.append(String.format("%-20s %-10s %-10s\n",
                        truncate(p.getName(), 20),
                        p.isAlive() ? "运行中" : "已停止",
                        formatTime(p.getRunningTime())));
            }
        }
        
        if (foreground.isEmpty() && background.isEmpty()) {
            sb.append("当前没有运行中的进程\n");
        }
        
        return sb.toString();
    }

    /**
     * kill - 终止进程
     */
    private static String kill(String[] args) {
        if (args.length == 0) {
            return "用法: kill <进程ID或名称>";
        }

        ProcessManager pm = ProcessManager.getInstance();
        String target = args[0];

        // 尝试作为 PID 处理
        try {
            int pid = Integer.parseInt(target);
            if (pm.killProcess(pid)) {
                return "已终止进程: " + pid;
            } else {
                return "找不到进程: " + pid;
            }
        } catch (NumberFormatException e) {
            // 作为进程名处理
            if (pm.killBackgroundProcess(target)) {
                return "已终止后台进程: " + target;
            } else {
                return "找不到进程: " + target;
            }
        }
    }

    /**
     * bg - 将进程放入后台
     */
    private static String bg(String[] args) {
        // TODO: 实现后台功能
        return "后台运行功能尚未实现";
    }

    /**
     * fg - 将进程放入前台
     */
    private static String fg(String[] args) {
        // TODO: 实现前台功能
        return "前台运行功能尚未实现";
    }

    /**
     * env - 显示环境变量
     */
    private static String env(String[] args) {
        Map<String, String> envVars = System.getenv();
        StringBuilder sb = new StringBuilder();
        
        if (args.length > 0) {
            // 显示指定的环境变量
            String value = envVars.get(args[0]);
            if (value != null) {
                sb.append(args[0]).append("=").append(value);
            } else {
                sb.append("未设置: ").append(args[0]);
            }
        } else {
            // 显示所有环境变量
            sb.append("=== 环境变量 ===\n\n");
            for (Map.Entry<String, String> entry : envVars.entrySet()) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
            }
        }
        
        return sb.toString();
    }

    /**
     * which - 查找命令位置
     */
    private static String which(String[] args) {
        if (args.length == 0) {
            return "用法: which <命令>";
        }

        EnvManager envManager = EnvManager.getInstance();
        String command = args[0];
        
        // 查找解释器
        File interpreter = envManager.getInterpreter(command);
        if (interpreter != null) {
            return command + ": " + interpreter.getAbsolutePath();
        }
        
        // 在 PATH 中查找
        String path = System.getenv("PATH");
        if (path != null) {
            for (String dir : path.split(File.pathSeparator)) {
                File file = new File(dir, command);
                if (file.exists() && file.canExecute()) {
                    return command + ": " + file.getAbsolutePath();
                }
            }
        }
        
        return command + ": 未找到";
    }

    /**
     * clear - 清屏
     */
    private static String clear() {
        // 返回 ANSI 清屏序列
        return "\033[2J\033[H";
    }

    /**
     * help - 显示帮助
     */
    private static String help(String[] args) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== BinRunner 终端帮助 ===\n\n");
        
        sb.append("内置命令:\n");
        sb.append("  ps          显示进程列表\n");
        sb.append("  kill <id>   终止指定进程\n");
        sb.append("  bg <id>     将进程放入后台运行\n");
        sb.append("  fg <id>     将进程放入前台运行\n");
        sb.append("  env [name]  显示环境变量\n");
        sb.append("  which <cmd> 查找命令位置\n");
        sb.append("  clear       清屏\n");
        sb.append("  help        显示帮助\n");
        sb.append("  exit        退出终端\n");
        sb.append("\n");
        
        sb.append("支持的脚本类型:\n");
        sb.append("  .js   - QuickJS/JavaScript\n");
        sb.append("  .py   - Python\n");
        sb.append("  .php  - PHP\n");
        sb.append("  .lua  - Lua\n");
        sb.append("\n");
        
        sb.append("可用解释器:\n");
        EnvManager envManager = EnvManager.getInstance();
        for (Map.Entry<String, File> entry : envManager.getInterpreters().entrySet()) {
            sb.append("  ").append(entry.getKey()).append(" -> ").append(entry.getValue().getAbsolutePath()).append("\n");
        }
        
        return sb.toString();
    }

    /**
     * exit - 退出终端
     */
    private static String exit(Terminal terminal) {
        if (terminal != null) {
            terminal.close();
        }
        return "再见!";
    }

    /**
     * 截断字符串
     */
    private static String truncate(String str, int maxLen) {
        if (str == null) return "";
        if (str.length() <= maxLen) return str;
        return str.substring(0, maxLen - 3) + "...";
    }

    /**
     * 格式化时间
     */
    private static String formatTime(long millis) {
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
}
