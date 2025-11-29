package com.github.catvod.binrunner.terminal;

import com.github.catvod.binrunner.core.EnvManager;
import com.github.catvod.binrunner.core.ProcessManager;
import com.github.catvod.binrunner.process.BinProcess;
import com.github.catvod.binrunner.util.EnvBuilder;

import java.util.List;
import java.util.Map;

/**
 * Builtin command implementations for BinRunner terminal.
 * Handles internal terminal commands like ps, kill, bg, fg, env, which, etc.
 */
public class BuiltinCommands {

    private final ProcessManager processManager;
    private final EnvManager envManager;

    public BuiltinCommands(ProcessManager processManager, EnvManager envManager) {
        this.processManager = processManager;
        this.envManager = envManager;
    }

    /**
     * Execute a builtin command.
     *
     * @param parsed parsed command
     * @return command output
     */
    public String execute(CommandParser.ParsedCommand parsed) {
        String cmd = parsed.command.toLowerCase();
        
        switch (cmd) {
            case "ps":
                return executePs();
            case "kill":
                return executeKill(parsed.args);
            case "bg":
                return executeBg();
            case "fg":
                return executeFg(parsed.args);
            case "env":
                return executeEnv();
            case "which":
                return executeWhich(parsed.args);
            case "clear":
                return executeClear();
            case "help":
                return executeHelp();
            case "exit":
            case "quit":
                return executeExit();
            default:
                return "Unknown builtin command: " + cmd;
        }
    }

    /**
     * List all running processes.
     *
     * @return formatted process list
     */
    private String executePs() {
        List<BinProcess> processes = processManager.getAllProcesses();
        if (processes.isEmpty()) {
            return "No processes running.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-6s %-8s %-10s %-10s %s%n", 
            "ID", "PID", "STATE", "UPTIME", "COMMAND"));
        sb.append("------------------------------------------------------\n");
        
        for (BinProcess p : processes) {
            sb.append(String.format("%-6d %-8s %-10s %-10s %s%n",
                p.getId(),
                p.getPid() > 0 ? String.valueOf(p.getPid()) : "-",
                p.getState().name(),
                p.getFormattedUptime(),
                p.getName()));
        }
        
        sb.append("\n").append(processManager.getStatus());
        return sb.toString();
    }

    /**
     * Kill a process by ID.
     *
     * @param args command arguments
     * @return result message
     */
    private String executeKill(List<String> args) {
        if (args.isEmpty()) {
            return "Usage: kill <pid>";
        }

        try {
            int id = Integer.parseInt(args.get(0));
            if (processManager.kill(id)) {
                return "Process " + id + " terminated.";
            } else {
                return "Process " + id + " not found.";
            }
        } catch (NumberFormatException e) {
            return "Invalid process ID: " + args.get(0);
        }
    }

    /**
     * List background processes.
     *
     * @return formatted background process list
     */
    private String executeBg() {
        List<BinProcess> bgProcesses = processManager.getBackgroundProcesses();
        if (bgProcesses.isEmpty()) {
            return "No background processes.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Background Processes:\n");
        sb.append(String.format("%-6s %-8s %-10s %s%n", "ID", "PID", "UPTIME", "COMMAND"));
        sb.append("------------------------------------------\n");
        
        for (BinProcess p : bgProcesses) {
            sb.append(String.format("%-6d %-8s %-10s %s%n",
                p.getId(),
                p.getPid() > 0 ? String.valueOf(p.getPid()) : "-",
                p.getFormattedUptime(),
                p.getName()));
        }
        return sb.toString();
    }

    /**
     * Bring a background process to foreground.
     *
     * @param args command arguments
     * @return result message
     */
    private String executeFg(List<String> args) {
        if (args.isEmpty()) {
            return "Usage: fg <pid>";
        }

        try {
            int id = Integer.parseInt(args.get(0));
            BinProcess process = processManager.getProcess(id);
            if (process == null) {
                return "Process " + id + " not found.";
            }
            if (!process.isAlive()) {
                return "Process " + id + " is not running.";
            }
            if (!process.isBackground()) {
                return "Process " + id + " is already in foreground.";
            }
            
            // In a real implementation, this would attach to the process
            // For now, just return the output so far
            String output = process.getLastOutput();
            if (output != null && !output.isEmpty()) {
                return "Process " + id + " output:\n" + output;
            }
            return "Process " + id + " is running in foreground. (Press Ctrl+C to return)";
            
        } catch (NumberFormatException e) {
            return "Invalid process ID: " + args.get(0);
        }
    }

    /**
     * Show current environment variables.
     *
     * @return formatted environment variables
     */
    private String executeEnv() {
        EnvBuilder envBuilder = EnvBuilder.create();
        Map<String, String> env = envBuilder.build();
        
        StringBuilder sb = new StringBuilder();
        sb.append("Environment Variables:\n");
        
        // Show important ones first
        String[] important = {"PATH", "HOME", "LD_LIBRARY_PATH", "TMPDIR"};
        for (String key : important) {
            String value = env.get(key);
            if (value != null) {
                sb.append(key).append("=").append(value).append("\n");
            }
        }
        
        sb.append("\n(Use 'env | grep <pattern>' for full list)");
        return sb.toString();
    }

    /**
     * Find command path.
     *
     * @param args command arguments
     * @return command path or not found message
     */
    private String executeWhich(List<String> args) {
        if (args.isEmpty()) {
            return "Usage: which <command>";
        }

        String cmdName = args.get(0);
        
        // Check if it's a builtin
        if (CommandParser.isBuiltin(cmdName)) {
            return cmdName + ": shell builtin command";
        }
        
        // Check registered commands
        String path = envManager.getCommandPath(cmdName);
        if (path != null) {
            return path;
        }
        
        return cmdName + ": not found";
    }

    /**
     * Clear screen (returns special marker).
     *
     * @return clear screen marker
     */
    private String executeClear() {
        // Return a special marker that the terminal UI can interpret
        return "\033[2J\033[H";
    }

    /**
     * Show help information.
     *
     * @return help text
     */
    private String executeHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append("BinRunner Terminal - Help\n");
        sb.append("=========================\n\n");
        sb.append("Builtin Commands:\n");
        sb.append("  ps              List all running processes\n");
        sb.append("  kill <pid>      Terminate a process by ID\n");
        sb.append("  bg              List background processes\n");
        sb.append("  fg <pid>        Bring a background process to foreground\n");
        sb.append("  env             Show environment variables\n");
        sb.append("  which <cmd>     Find command path\n");
        sb.append("  clear           Clear screen\n");
        sb.append("  help            Show this help\n");
        sb.append("  exit            Exit terminal\n\n");
        sb.append("Usage:\n");
        sb.append("  command args    Execute a command\n");
        sb.append("  command &       Execute in background\n");
        sb.append("  ./script.php    Run script (auto-detect interpreter)\n\n");
        sb.append("Available Commands:\n");
        
        Map<String, EnvManager.CommandInfo> commands = envManager.getAllCommands();
        if (commands.isEmpty()) {
            sb.append("  (no commands registered)\n");
        } else {
            for (String name : commands.keySet()) {
                sb.append("  ").append(name).append("\n");
            }
        }
        
        return sb.toString();
    }

    /**
     * Exit terminal.
     *
     * @return exit message
     */
    private String executeExit() {
        // Return a special marker for exit
        return "EXIT";
    }
}
