package com.github.catvod.binrunner.util;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 日志工具类
 * 提供统一的日志输出格式
 */
public class BinLogger {

    private static final String TAG = "BinRunner";
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    /**
     * 输出调试日志
     * @param message 日志内容
     */
    public static void debug(String message) {
        Log.d(TAG, formatMessage(message));
    }

    /**
     * 输出信息日志
     * @param message 日志内容
     */
    public static void info(String message) {
        Log.i(TAG, formatMessage(message));
    }

    /**
     * 输出警告日志
     * @param message 日志内容
     */
    public static void warn(String message) {
        Log.w(TAG, formatMessage(message));
    }

    /**
     * 输出错误日志
     * @param message 日志内容
     */
    public static void error(String message) {
        Log.e(TAG, formatMessage(message));
    }

    /**
     * 输出错误日志（带异常）
     * @param message 日志内容
     * @param e 异常对象
     */
    public static void error(String message, Throwable e) {
        Log.e(TAG, formatMessage(message), e);
    }

    /**
     * 输出命令执行日志
     * @param command 执行的命令
     */
    public static void command(String command) {
        Log.d(TAG, "[" + getTimeStamp() + "] " + command);
    }

    /**
     * 输出命令执行结果
     * @param output 命令输出
     */
    public static void output(String output) {
        if (output != null && !output.isEmpty()) {
            Log.d(TAG, output);
        }
    }

    /**
     * 格式化日志消息
     * @param message 原始消息
     * @return 格式化后的消息
     */
    private static String formatMessage(String message) {
        return "[" + getTimeStamp() + "] " + message;
    }

    /**
     * 获取当前时间戳
     * @return 时间戳字符串
     */
    private static String getTimeStamp() {
        return TIME_FORMAT.format(new Date());
    }
}
