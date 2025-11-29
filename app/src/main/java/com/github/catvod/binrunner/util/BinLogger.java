package com.github.catvod.binrunner.util;

import com.github.catvod.binrunner.config.LogConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Logger utility for BinRunner.
 * Handles log file creation, rotation, and formatted output.
 * Thread-safe implementation.
 */
public class BinLogger {

    private static final String TAG = "BinLogger";
    private static final String DATE_PATTERN = "yyyy_MM_dd";
    private static final String TIME_PATTERN = "HH:mm:ss";

    private final LogConfig config;
    private final File logDir;
    private final Object lock = new Object();
    private File currentLogFile;
    private PrintWriter writer;
    private boolean initialized;

    public BinLogger(LogConfig config) {
        this.config = config;
        this.logDir = new File(config.getPath());
        this.initialized = false;
    }

    /**
     * Get thread-safe date formatter.
     */
    private String formatDate(Date date) {
        return new SimpleDateFormat(DATE_PATTERN, Locale.US).format(date);
    }

    /**
     * Get thread-safe time formatter.
     */
    private String formatTime(Date date) {
        return new SimpleDateFormat(TIME_PATTERN, Locale.US).format(date);
    }

    /**
     * Initialize the logger.
     * Creates log directory and opens the log file.
     *
     * @return true if initialization successful
     */
    public boolean init() {
        if (!config.isEnable()) {
            return true;
        }
        
        synchronized (lock) {
            try {
                if (!logDir.exists() && !logDir.mkdirs()) {
                    return false;
                }
                
                currentLogFile = getLogFile();
                checkRotationLocked();
                openWriterLocked();
                initialized = true;
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    /**
     * Log a command execution.
     *
     * @param command the command that was executed
     * @param output the command output
     */
    public void log(String command, String output) {
        if (!config.isEnable() || !initialized) {
            return;
        }
        
        synchronized (lock) {
            try {
                checkRotationLocked();
                
                String timestamp = formatTime(new Date());
                if (writer != null) {
                    writer.printf("[%s] %s%n", timestamp, command);
                    if (output != null && !output.isEmpty()) {
                        writer.println(output);
                        writer.println();
                    }
                    writer.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Log an info message.
     *
     * @param message the message to log
     */
    public void info(String message) {
        if (!config.isEnable() || !initialized) {
            return;
        }
        
        synchronized (lock) {
            try {
                checkRotationLocked();
                
                String timestamp = formatTime(new Date());
                if (writer != null) {
                    writer.printf("[%s] [INFO] %s%n", timestamp, message);
                    writer.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Log an error message.
     *
     * @param message the error message
     * @param error the exception (optional)
     */
    public void error(String message, Throwable error) {
        if (!config.isEnable() || !initialized) {
            return;
        }
        
        synchronized (lock) {
            try {
                checkRotationLocked();
                
                String timestamp = formatTime(new Date());
                if (writer != null) {
                    writer.printf("[%s] [ERROR] %s%n", timestamp, message);
                    if (error != null) {
                        error.printStackTrace(writer);
                    }
                    writer.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Check if log rotation is needed and rotate if necessary.
     * Must be called within synchronized block.
     */
    private void checkRotationLocked() {
        if (currentLogFile == null || !currentLogFile.exists()) {
            currentLogFile = getLogFile();
            openWriterLocked();
            return;
        }
        
        long sizeKB = currentLogFile.length() / 1024;
        if (sizeKB >= config.getMaxSize()) {
            rotateLocked();
        }
    }

    /**
     * Rotate the log file.
     * Must be called within synchronized block.
     */
    private void rotateLocked() {
        closeWriterLocked();
        
        String baseName = formatDate(new Date());
        int index = 1;
        
        File rotatedFile;
        do {
            rotatedFile = new File(logDir, baseName + "_" + index + ".log");
            index++;
        } while (rotatedFile.exists());
        
        if (currentLogFile.renameTo(rotatedFile)) {
            currentLogFile = getLogFile();
            openWriterLocked();
        }
    }

    /**
     * Get current log file.
     *
     * @return log file for today
     */
    private File getLogFile() {
        String fileName = formatDate(new Date()) + ".log";
        return new File(logDir, fileName);
    }

    /**
     * Open the log writer.
     * Must be called within synchronized block.
     */
    private void openWriterLocked() {
        try {
            closeWriterLocked();
            writer = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(currentLogFile, true), StandardCharsets.UTF_8), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Close the log writer.
     * Must be called within synchronized block.
     */
    private void closeWriterLocked() {
        if (writer != null) {
            writer.close();
            writer = null;
        }
    }

    /**
     * Close the logger and release resources.
     */
    public void close() {
        synchronized (lock) {
            closeWriterLocked();
            initialized = false;
        }
    }
}
