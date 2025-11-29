package com.github.catvod.binrunner.core;

import com.github.catvod.binrunner.config.EnvsConfig;
import com.github.catvod.binrunner.process.BinProcess;
import com.github.catvod.binrunner.process.ProcessResult;
import com.github.catvod.binrunner.process.ProcessState;
import com.github.catvod.binrunner.util.BinLogger;
import com.github.catvod.binrunner.util.EnvBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Binary executor for BinRunner.
 * Handles process building, environment setup, and command execution.
 */
public class BinExecutor {

    private final EnvManager envManager;
    private final ProcessManager processManager;
    private final EnvsConfig config;
    private final BinLogger logger;

    public BinExecutor(EnvManager envManager, ProcessManager processManager, 
                       EnvsConfig config, BinLogger logger) {
        this.envManager = envManager;
        this.processManager = processManager;
        this.config = config;
        this.logger = logger;
    }

    /**
     * Execute a command and wait for completion.
     *
     * @param command command string
     * @return execution result
     */
    public ProcessResult execute(String command) {
        return execute(command, config.getSettings().getTimeout());
    }

    /**
     * Execute a command with custom timeout.
     *
     * @param command command string
     * @param timeoutSeconds timeout in seconds (0 = no timeout)
     * @return execution result
     */
    public ProcessResult execute(String command, int timeoutSeconds) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Parse command
            List<String> cmdList = parseCommand(command);
            if (cmdList.isEmpty()) {
                return ProcessResult.error("Empty command", -1, 0);
            }

            // Get the executable
            String executable = cmdList.get(0);
            
            // Check if it's a script file
            File scriptFile = new File(executable);
            if (scriptFile.exists() && scriptFile.isFile()) {
                String interpreter = envManager.findInterpreter(executable);
                if (interpreter != null) {
                    cmdList.add(0, getExecutablePath(interpreter));
                } else {
                    cmdList.set(0, scriptFile.getAbsolutePath());
                }
            } else {
                // Try to resolve as registered command
                String execPath = getExecutablePath(executable);
                if (execPath != null) {
                    cmdList.set(0, execPath);
                }
            }

            // Build process
            ProcessBuilder pb = createProcessBuilder(cmdList);
            
            // Start process
            Process process = pb.start();
            
            // Wait for completion with timeout
            boolean completed;
            if (timeoutSeconds > 0) {
                completed = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
                if (!completed) {
                    process.destroyForcibly();
                    return ProcessResult.timeout(timeoutSeconds);
                }
            } else {
                process.waitFor();
                completed = true;
            }

            // Read output
            String output = readStream(process.getInputStream());
            String error = readStream(process.getErrorStream());
            int exitCode = process.exitValue();

            long executionTime = System.currentTimeMillis() - startTime;
            ProcessResult result = ProcessResult.of(output, error, exitCode, executionTime);
            
            // Log the execution
            logger.log(command, result.getCombinedOutput());
            
            return result;

        } catch (IOException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            String errorMsg = "Failed to execute command: " + e.getMessage();
            logger.error(errorMsg, e);
            return ProcessResult.error(errorMsg, -1, executionTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            long executionTime = System.currentTimeMillis() - startTime;
            return ProcessResult.error("Command interrupted", -1, executionTime);
        }
    }

    /**
     * Execute a command in background.
     *
     * @param command command string
     * @param name optional process name
     * @return BinProcess handle
     */
    public BinProcess executeBackground(String command, String name) {
        return executeBackground(command, name, false, 0);
    }

    /**
     * Execute a command in background with auto-restart option.
     *
     * @param command command string
     * @param name optional process name
     * @param autoRestart enable auto-restart
     * @param maxRestarts max restart count
     * @return BinProcess handle or null if failed
     */
    public BinProcess executeBackground(String command, String name, 
                                        boolean autoRestart, int maxRestarts) {
        if (!processManager.canStartProcess()) {
            logger.error("Max process limit reached", null);
            return null;
        }

        try {
            // Parse command
            List<String> cmdList = parseCommand(command);
            if (cmdList.isEmpty()) {
                return null;
            }

            // Resolve executable
            String executable = cmdList.get(0);
            File scriptFile = new File(executable);
            if (scriptFile.exists() && scriptFile.isFile()) {
                String interpreter = envManager.findInterpreter(executable);
                if (interpreter != null) {
                    cmdList.add(0, getExecutablePath(interpreter));
                } else {
                    cmdList.set(0, scriptFile.getAbsolutePath());
                }
            } else {
                String execPath = getExecutablePath(executable);
                if (execPath != null) {
                    cmdList.set(0, execPath);
                }
            }

            // Create process wrapper
            int id = processManager.generateId();
            BinProcess binProcess = new BinProcess(id, command, 
                name != null ? name : command, true, autoRestart, maxRestarts);

            // Build and start process
            ProcessBuilder pb = createProcessBuilder(cmdList);
            Process process = pb.start();
            binProcess.setProcess(process);
            binProcess.setState(ProcessState.RUNNING);

            // Register with process manager
            processManager.register(binProcess);
            
            logger.info("Started background process [" + id + "]: " + command);
            return binProcess;

        } catch (IOException e) {
            logger.error("Failed to start background process: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Restart a process.
     *
     * @param binProcess process to restart
     * @return true if restarted successfully
     */
    public boolean restart(BinProcess binProcess) {
        if (!binProcess.canRestart()) {
            return false;
        }

        try {
            // Parse original command
            List<String> cmdList = parseCommand(binProcess.getCommand());
            if (cmdList.isEmpty()) {
                return false;
            }

            // Resolve executable
            String executable = cmdList.get(0);
            String execPath = getExecutablePath(executable);
            if (execPath != null) {
                cmdList.set(0, execPath);
            }

            // Build and start new process
            ProcessBuilder pb = createProcessBuilder(cmdList);
            Process process = pb.start();
            
            binProcess.setProcess(process);
            binProcess.setState(ProcessState.RUNNING);
            binProcess.incrementRestartCount();
            
            logger.info("Restarted process [" + binProcess.getId() + "] " +
                "(restart " + binProcess.getRestartCount() + "/" + binProcess.getMaxRestarts() + ")");
            return true;

        } catch (IOException e) {
            binProcess.setState(ProcessState.ERROR);
            logger.error("Failed to restart process: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Create a ProcessBuilder with proper environment.
     *
     * @param cmdList command list
     * @return configured ProcessBuilder
     */
    private ProcessBuilder createProcessBuilder(List<String> cmdList) {
        ProcessBuilder pb = new ProcessBuilder(cmdList);
        pb.redirectErrorStream(false);
        
        // Set working directory to envs directory
        pb.directory(envManager.getEnvsDir());

        // Build environment
        EnvBuilder envBuilder = EnvBuilder.create();
        
        // Get command info for library path
        if (!cmdList.isEmpty()) {
            String execName = new File(cmdList.get(0)).getName();
            EnvManager.CommandInfo cmdInfo = envManager.getCommand(execName);
            if (cmdInfo != null && cmdInfo.libPath != null) {
                envBuilder.setLibraryPath(cmdInfo.libPath);
            }
        }

        // Set HOME and TMPDIR
        envBuilder.setHome(envManager.getEnvsDir().getAbsolutePath());
        envBuilder.setTmpDir(System.getProperty("java.io.tmpdir"));

        // Apply environment
        pb.environment().putAll(envBuilder.build());

        return pb;
    }

    /**
     * Get executable path for a command name.
     *
     * @param name command name
     * @return full path or null
     */
    private String getExecutablePath(String name) {
        EnvManager.CommandInfo info = envManager.getCommand(name);
        return info != null ? info.binPath : null;
    }

    /**
     * Parse command string into list.
     *
     * @param command command string
     * @return list of command parts
     */
    private List<String> parseCommand(String command) {
        List<String> result = new ArrayList<>();
        if (command == null || command.trim().isEmpty()) {
            return result;
        }

        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        char quoteChar = 0;

        for (int i = 0; i < command.length(); i++) {
            char c = command.charAt(i);

            if (inQuotes) {
                if (c == quoteChar) {
                    inQuotes = false;
                } else {
                    current.append(c);
                }
            } else {
                if (c == '"' || c == '\'') {
                    inQuotes = true;
                    quoteChar = c;
                } else if (Character.isWhitespace(c)) {
                    if (current.length() > 0) {
                        result.add(current.toString());
                        current = new StringBuilder();
                    }
                } else {
                    current.append(c);
                }
            }
        }

        if (current.length() > 0) {
            result.add(current.toString());
        }

        return result;
    }

    /**
     * Read input stream to string.
     *
     * @param stream input stream
     * @return string content
     */
    private String readStream(java.io.InputStream stream) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append(line);
            }
        } catch (IOException e) {
            // Stream may be closed
        }
        return sb.toString();
    }
}
