package com.github.catvod.binrunner.core;

import com.github.catvod.binrunner.config.EnvsConfig;
import com.github.catvod.binrunner.util.FileHelper;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Environment manager for BinRunner.
 * Handles binary scanning, copying to private directory, and command registration.
 */
public class EnvManager {

    private static final String ENVS_DIR_NAME = "envs";
    private static final String BIN_DIR_NAME = "bin";
    private static final String LIB_DIR_NAME = "lib";
    private static final String PRIVATE_DIR_NAME = "binrunner";

    private final File envsDir;
    private final File privateDir;
    private final EnvsConfig config;
    private final Map<String, CommandInfo> commands;
    private boolean initialized;

    /**
     * Command information including path and library path.
     */
    public static class CommandInfo {
        public final String name;
        public final String binPath;
        public final String libPath;
        public final String envType;

        public CommandInfo(String name, String binPath, String libPath, String envType) {
            this.name = name;
            this.binPath = binPath;
            this.libPath = libPath;
            this.envType = envType;
        }
    }

    public EnvManager(File envsDir, File privateDir, EnvsConfig config) {
        this.envsDir = envsDir;
        this.privateDir = new File(privateDir, PRIVATE_DIR_NAME);
        this.config = config;
        this.commands = new HashMap<>();
        this.initialized = false;
    }

    /**
     * Initialize the environment manager.
     * Copies binaries to private directory, sets permissions, and scans commands.
     *
     * @return true if initialization successful
     */
    public boolean init() {
        if (!envsDir.exists()) {
            return false;
        }

        // Create private directory
        if (!privateDir.exists() && !privateDir.mkdirs()) {
            return false;
        }

        // Copy and scan each environment subdirectory
        File[] envDirs = envsDir.listFiles(File::isDirectory);
        if (envDirs == null) {
            initialized = true;
            return true;
        }

        for (File envDir : envDirs) {
            if (envDir.getName().equals("logs")) {
                continue; // Skip logs directory
            }
            processEnvDirectory(envDir);
        }

        initialized = true;
        return true;
    }

    /**
     * Process an environment directory (php, python, node, etc.).
     *
     * @param envDir environment directory
     */
    private void processEnvDirectory(File envDir) {
        File binDir = new File(envDir, BIN_DIR_NAME);
        File libDir = new File(envDir, LIB_DIR_NAME);

        if (!binDir.exists() || !binDir.isDirectory()) {
            return;
        }

        // Create corresponding private directory
        File privateEnvDir = new File(privateDir, envDir.getName());
        File privateBinDir = new File(privateEnvDir, BIN_DIR_NAME);
        File privateLibDir = new File(privateEnvDir, LIB_DIR_NAME);

        // Copy bin directory
        if (!privateBinDir.exists()) {
            privateBinDir.mkdirs();
        }
        FileHelper.copyDirectory(binDir, privateBinDir);
        FileHelper.setExecutableDirectory(privateBinDir);

        // Copy lib directory if exists
        String libPath = null;
        if (libDir.exists() && libDir.isDirectory()) {
            if (!privateLibDir.exists()) {
                privateLibDir.mkdirs();
            }
            FileHelper.copyDirectory(libDir, privateLibDir);
            libPath = privateLibDir.getAbsolutePath();
        }

        // Register all commands in bin directory
        File[] binFiles = privateBinDir.listFiles(File::isFile);
        if (binFiles != null) {
            for (File binFile : binFiles) {
                String cmdName = binFile.getName();
                commands.put(cmdName, new CommandInfo(
                    cmdName,
                    binFile.getAbsolutePath(),
                    libPath,
                    envDir.getName()
                ));
            }
        }
    }

    /**
     * Check if environment is initialized.
     *
     * @return true if initialized
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Get command information by name.
     *
     * @param name command name
     * @return CommandInfo or null if not found
     */
    public CommandInfo getCommand(String name) {
        // Resolve alias first
        String resolved = config.resolveAlias(name);
        return commands.get(resolved);
    }

    /**
     * Check if a command exists.
     *
     * @param name command name
     * @return true if command exists
     */
    public boolean hasCommand(String name) {
        String resolved = config.resolveAlias(name);
        return commands.containsKey(resolved);
    }

    /**
     * Get all registered commands.
     *
     * @return map of command name to CommandInfo
     */
    public Map<String, CommandInfo> getAllCommands() {
        return new HashMap<>(commands);
    }

    /**
     * Get command path by name.
     *
     * @param name command name
     * @return full path to command or null
     */
    public String getCommandPath(String name) {
        CommandInfo info = getCommand(name);
        return info != null ? info.binPath : null;
    }

    /**
     * Find interpreter for a script file based on extension.
     *
     * @param scriptPath path to script file
     * @return interpreter command name or null
     */
    public String findInterpreter(String scriptPath) {
        String ext = FileHelper.getExtension(scriptPath);
        
        switch (ext) {
            case "php":
                return resolveInterpreter("php");
            case "py":
                return resolveInterpreter("python3", "python", "py");
            case "js":
                return resolveInterpreter("node", "nodejs");
            case "lua":
                return resolveInterpreter("lua");
            case "sh":
                return "sh";
            default:
                return null;
        }
    }

    /**
     * Resolve interpreter from possible names.
     *
     * @param names possible interpreter names in priority order
     * @return first found interpreter or first name
     */
    private String resolveInterpreter(String... names) {
        for (String name : names) {
            // Check alias first
            String resolved = config.resolveAlias(name);
            if (commands.containsKey(resolved)) {
                return resolved;
            }
            if (commands.containsKey(name)) {
                return name;
            }
        }
        return names.length > 0 ? names[0] : null;
    }

    /**
     * Get the envs directory.
     *
     * @return envs directory file
     */
    public File getEnvsDir() {
        return envsDir;
    }

    /**
     * Get the private directory.
     *
     * @return private directory file
     */
    public File getPrivateDir() {
        return privateDir;
    }

    /**
     * Reload commands by rescanning directories.
     *
     * @return true if successful
     */
    public boolean reload() {
        commands.clear();
        initialized = false;
        return init();
    }
}
