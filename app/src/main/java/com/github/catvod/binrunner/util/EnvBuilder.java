package com.github.catvod.binrunner.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Environment variable builder for process execution.
 * Constructs the environment map with proper LD_LIBRARY_PATH and PATH settings.
 */
public class EnvBuilder {

    private final Map<String, String> env;

    public EnvBuilder() {
        this.env = new HashMap<>();
    }

    /**
     * Create a new EnvBuilder with system environment.
     *
     * @return new EnvBuilder with inherited environment
     */
    public static EnvBuilder create() {
        EnvBuilder builder = new EnvBuilder();
        builder.env.putAll(System.getenv());
        return builder;
    }

    /**
     * Set LD_LIBRARY_PATH for native library loading.
     *
     * @param paths library paths to add
     * @return this builder
     */
    public EnvBuilder setLibraryPath(String... paths) {
        if (paths == null || paths.length == 0) {
            return this;
        }
        
        StringBuilder sb = new StringBuilder();
        for (String path : paths) {
            if (path != null && !path.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append(File.pathSeparator);
                }
                sb.append(path);
            }
        }
        
        // Append existing LD_LIBRARY_PATH if present
        String existing = env.get("LD_LIBRARY_PATH");
        if (existing != null && !existing.isEmpty()) {
            sb.append(File.pathSeparator).append(existing);
        }
        
        env.put("LD_LIBRARY_PATH", sb.toString());
        return this;
    }

    /**
     * Add paths to PATH environment variable.
     *
     * @param paths paths to add
     * @return this builder
     */
    public EnvBuilder addPath(String... paths) {
        if (paths == null || paths.length == 0) {
            return this;
        }
        
        StringBuilder sb = new StringBuilder();
        for (String path : paths) {
            if (path != null && !path.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append(File.pathSeparator);
                }
                sb.append(path);
            }
        }
        
        // Append existing PATH
        String existing = env.get("PATH");
        if (existing != null && !existing.isEmpty()) {
            sb.append(File.pathSeparator).append(existing);
        }
        
        env.put("PATH", sb.toString());
        return this;
    }

    /**
     * Set a single environment variable.
     *
     * @param name variable name
     * @param value variable value
     * @return this builder
     */
    public EnvBuilder set(String name, String value) {
        if (name != null && value != null) {
            env.put(name, value);
        }
        return this;
    }

    /**
     * Set HOME environment variable.
     *
     * @param home home directory path
     * @return this builder
     */
    public EnvBuilder setHome(String home) {
        return set("HOME", home);
    }

    /**
     * Set TMPDIR environment variable.
     *
     * @param tmpDir temp directory path
     * @return this builder
     */
    public EnvBuilder setTmpDir(String tmpDir) {
        return set("TMPDIR", tmpDir);
    }

    /**
     * Remove an environment variable.
     *
     * @param name variable name
     * @return this builder
     */
    public EnvBuilder remove(String name) {
        if (name != null) {
            env.remove(name);
        }
        return this;
    }

    /**
     * Build the environment map.
     *
     * @return environment variable map
     */
    public Map<String, String> build() {
        return new HashMap<>(env);
    }

    /**
     * Convert to String array for ProcessBuilder.
     *
     * @return environment as String array
     */
    public String[] toArray() {
        String[] result = new String[env.size()];
        int i = 0;
        for (Map.Entry<String, String> entry : env.entrySet()) {
            result[i++] = entry.getKey() + "=" + entry.getValue();
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Environment:\n");
        for (Map.Entry<String, String> entry : env.entrySet()) {
            sb.append("  ").append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }
}
