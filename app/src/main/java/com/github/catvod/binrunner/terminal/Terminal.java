package com.github.catvod.binrunner.terminal;

import com.github.catvod.binrunner.core.BinExecutor;
import com.github.catvod.binrunner.core.EnvManager;
import com.github.catvod.binrunner.core.ProcessManager;
import com.github.catvod.binrunner.process.BinProcess;
import com.github.catvod.binrunner.process.ProcessResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Interactive terminal for BinRunner.
 * Handles user input, command dispatch, and command history.
 */
public class Terminal {

    private static final int MAX_HISTORY = 100;

    private final BinExecutor executor;
    private final BuiltinCommands builtins;
    private final List<String> history;
    private int historyIndex;
    private boolean running;

    public Terminal(BinExecutor executor, ProcessManager processManager, EnvManager envManager) {
        this.executor = executor;
        this.builtins = new BuiltinCommands(processManager, envManager);
        this.history = new ArrayList<>();
        this.historyIndex = -1;
        this.running = true;
    }

    /**
     * Process user input and return result.
     *
     * @param input user input string
     * @return command output
     */
    public String processInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }

        // Add to history
        addToHistory(input);

        // Parse input
        CommandParser.ParsedCommand parsed = CommandParser.parse(input);
        if (parsed.isEmpty()) {
            return "";
        }

        // Check for builtin command
        if (CommandParser.isBuiltin(parsed.command)) {
            String result = builtins.execute(parsed);
            
            // Check for exit
            if ("EXIT".equals(result)) {
                running = false;
                return "Goodbye!";
            }
            
            return result;
        }

        // Execute external command
        if (parsed.background) {
            return executeBackground(parsed);
        } else {
            return executeForeground(parsed);
        }
    }

    /**
     * Execute command in foreground.
     *
     * @param parsed parsed command
     * @return command output
     */
    private String executeForeground(CommandParser.ParsedCommand parsed) {
        ProcessResult result = executor.execute(parsed.getFullCommand());
        
        if (result.isSuccess()) {
            String output = result.getOutput();
            if (output.isEmpty() && result.getError().isEmpty()) {
                return "(command completed with exit code " + result.getExitCode() + ")";
            }
            return result.getCombinedOutput();
        } else {
            if (!result.getError().isEmpty()) {
                return "Error: " + result.getError();
            }
            return "Command failed with exit code " + result.getExitCode();
        }
    }

    /**
     * Execute command in background.
     *
     * @param parsed parsed command
     * @return status message
     */
    private String executeBackground(CommandParser.ParsedCommand parsed) {
        BinProcess process = executor.executeBackground(
            parsed.getFullCommand(), 
            parsed.command
        );
        
        if (process != null) {
            return "[" + process.getId() + "] " + process.getPid() + " " + parsed.command;
        } else {
            return "Failed to start background process";
        }
    }

    /**
     * Add input to command history.
     *
     * @param input input to add
     */
    private void addToHistory(String input) {
        if (input == null || input.trim().isEmpty()) {
            return;
        }
        
        // Don't add duplicate consecutive commands
        if (!history.isEmpty() && history.get(history.size() - 1).equals(input)) {
            return;
        }
        
        history.add(input);
        
        // Limit history size
        while (history.size() > MAX_HISTORY) {
            history.remove(0);
        }
        
        historyIndex = history.size();
    }

    /**
     * Get previous command from history.
     *
     * @return previous command or empty string
     */
    public String historyPrev() {
        if (history.isEmpty()) {
            return "";
        }
        
        if (historyIndex > 0) {
            historyIndex--;
        }
        
        return history.get(historyIndex);
    }

    /**
     * Get next command from history.
     *
     * @return next command or empty string
     */
    public String historyNext() {
        if (history.isEmpty()) {
            return "";
        }
        
        if (historyIndex < history.size() - 1) {
            historyIndex++;
            return history.get(historyIndex);
        }
        
        historyIndex = history.size();
        return "";
    }

    /**
     * Get command history.
     *
     * @return list of history entries
     */
    public List<String> getHistory() {
        return new ArrayList<>(history);
    }

    /**
     * Check if terminal is still running.
     *
     * @return true if running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Stop the terminal.
     */
    public void stop() {
        running = false;
    }

    /**
     * Get the prompt string.
     *
     * @return prompt string
     */
    public String getPrompt() {
        return "binrunner$ ";
    }

    /**
     * Get terminal welcome message.
     *
     * @return welcome message
     */
    public String getWelcome() {
        return "BinRunner Terminal v1.0\nType 'help' for available commands.\n";
    }
}
