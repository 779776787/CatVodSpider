package com.github.catvod.spider.binrunner.util;

import android.util.Log;

import com.github.catvod.spider.binrunner.config.LogConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 日志工具类
 * 提供日志记录功能，支持文件和控制台输出
 */
public class BinLogger {

    private static final String TAG = "BinRunner";
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private static BinLogger instance;
    private LogConfig config;
    private File logFile;

    private BinLogger() {
        this.config = new LogConfig();
    }

    /**
     * 获取单例实例
     */
    public static synchronized BinLogger getInstance() {
        if (instance == null) {
            instance = new BinLogger();
        }
        return instance;
    }

    /**
     * 初始化日志配置
     */
    public void init(LogConfig config) {
        this.config = config;
        if (config.isEnable()) {
            initLogFile();
        }
    }

    /**
     * 初始化日志文件
     */
    private void initLogFile() {
        try {
            File logDir = new File(config.getPath());
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            String fileName = "binrunner_" + DATE_FORMAT.format(new Date()) + ".log";
            logFile = new File(logDir, fileName);
            // 检查文件大小，如果超过最大值则清空
            if (logFile.exists() && logFile.length() > config.getMaxSize() * 1024L) {
                logFile.delete();
            }
        } catch (Exception e) {
            Log.e(TAG, "初始化日志文件失败: " + e.getMessage());
        }
    }

    /**
     * 记录调试日志
     */
    public static void d(String message) {
        getInstance().log("D", message);
    }

    /**
     * 记录信息日志
     */
    public static void i(String message) {
        getInstance().log("I", message);
    }

    /**
     * 记录警告日志
     */
    public static void w(String message) {
        getInstance().log("W", message);
    }

    /**
     * 记录错误日志
     */
    public static void e(String message) {
        getInstance().log("E", message);
    }

    /**
     * 记录错误日志（带异常）
     */
    public static void e(String message, Throwable t) {
        getInstance().log("E", message + ": " + t.getMessage());
    }

    /**
     * 记录命令执行日志
     * @param command 执行的命令
     * @param output 输出内容
     */
    public static void cmd(String command, String output) {
        getInstance().logCommand(command, output);
    }

    /**
     * 记录日志
     */
    private void log(String level, String message) {
        String time = TIME_FORMAT.format(new Date());
        String logMessage = String.format("[%s] [%s] %s", time, level, message);

        // 控制台输出
        switch (level) {
            case "D":
                Log.d(TAG, message);
                break;
            case "I":
                Log.i(TAG, message);
                break;
            case "W":
                Log.w(TAG, message);
                break;
            case "E":
                Log.e(TAG, message);
                break;
        }

        // 文件输出
        if (config.isEnable()) {
            writeToFile(logMessage);
        }
    }

    /**
     * 记录命令日志
     */
    private void logCommand(String command, String output) {
        String time = TIME_FORMAT.format(new Date());
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(time).append("] ").append(command).append("\n");
        if (output != null && !output.isEmpty()) {
            sb.append(output).append("\n");
        }

        Log.d(TAG, command);
        if (output != null && !output.isEmpty()) {
            Log.d(TAG, output);
        }

        if (config.isEnable()) {
            writeToFile(sb.toString());
        }
    }

    /**
     * 写入日志文件
     */
    private synchronized void writeToFile(String message) {
        if (logFile == null) {
            initLogFile();
        }
        if (logFile == null) {
            return;
        }

        try (FileOutputStream fos = new FileOutputStream(logFile, true);
             OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
            writer.write(message);
            writer.write("\n");
            writer.flush();
        } catch (Exception e) {
            Log.e(TAG, "写入日志文件失败: " + e.getMessage());
        }
    }

    /**
     * 清空日志文件
     */
    public void clear() {
        if (logFile != null && logFile.exists()) {
            logFile.delete();
        }
        initLogFile();
    }

    /**
     * 获取日志文件路径
     */
    public String getLogFilePath() {
        return logFile != null ? logFile.getAbsolutePath() : null;
    }
}
