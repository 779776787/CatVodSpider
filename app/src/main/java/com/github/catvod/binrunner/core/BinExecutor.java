package com.github.catvod.binrunner.core;

import com.github.catvod.binrunner.process.BinProcess;
import com.github.catvod.binrunner.process.ProcessResult;
import com.github.catvod.binrunner.util.BinLogger;
import com.github.catvod.binrunner.util.EnvBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 二进制执行器
 * 负责执行命令行程序和脚本
 */
public class BinExecutor {

    private static final long DEFAULT_TIMEOUT = 30000; // 默认超时30秒

    private final EnvManager envManager;
    private long timeout = DEFAULT_TIMEOUT;

    /**
     * 构造函数
     * @param envManager 环境管理器
     */
    public BinExecutor(EnvManager envManager) {
        this.envManager = envManager;
    }

    /**
     * 设置执行超时时间
     * @param timeout 超时时间（毫秒）
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /**
     * 执行命令
     * @param command 命令字符串
     * @return 执行结果
     */
    public ProcessResult execute(String command) {
        return execute(command, null, null);
    }

    /**
     * 执行命令
     * @param command 命令字符串
     * @param workDir 工作目录
     * @return 执行结果
     */
    public ProcessResult execute(String command, File workDir) {
        return execute(command, null, workDir);
    }

    /**
     * 执行命令
     * @param command 命令字符串
     * @param extraEnvs 额外环境变量
     * @param workDir 工作目录
     * @return 执行结果
     */
    public ProcessResult execute(String command, Map<String, String> extraEnvs, File workDir) {
        if (command == null || command.trim().isEmpty()) {
            return ProcessResult.failure(-1, "命令不能为空", 0);
        }

        BinLogger.command(command);

        // 解析命令
        String[] cmdArray = parseCommand(command);
        if (cmdArray.length == 0) {
            return ProcessResult.failure(-1, "无效的命令", 0);
        }

        // 构建环境变量
        EnvBuilder envBuilder = new EnvBuilder();
        envBuilder.inheritSystemEnv();
        
        if (envManager != null) {
            envBuilder.merge(envManager.getEnvMap());
        }
        if (extraEnvs != null) {
            envBuilder.merge(extraEnvs);
        }

        // 创建并执行进程
        BinProcess process = new BinProcess(cmdArray, envBuilder.build(), workDir);
        ProcessResult result = process.execute(timeout);

        // 输出结果
        BinLogger.output(result.getAllOutput());

        return result;
    }

    /**
     * 执行命令数组
     * @param cmdArray 命令数组
     * @param extraEnvs 额外环境变量
     * @param workDir 工作目录
     * @return 执行结果
     */
    public ProcessResult execute(String[] cmdArray, Map<String, String> extraEnvs, File workDir) {
        if (cmdArray == null || cmdArray.length == 0) {
            return ProcessResult.failure(-1, "命令不能为空", 0);
        }

        BinLogger.command(String.join(" ", cmdArray));

        // 构建环境变量
        EnvBuilder envBuilder = new EnvBuilder();
        envBuilder.inheritSystemEnv();
        
        if (envManager != null) {
            envBuilder.merge(envManager.getEnvMap());
        }
        if (extraEnvs != null) {
            envBuilder.merge(extraEnvs);
        }

        // 创建并执行进程
        BinProcess process = new BinProcess(cmdArray, envBuilder.build(), workDir);
        ProcessResult result = process.execute(timeout);

        // 输出结果
        BinLogger.output(result.getAllOutput());

        return result;
    }

    /**
     * 执行脚本
     * @param interpreter 解释器路径
     * @param scriptPath 脚本路径
     * @param args 脚本参数
     * @return 执行结果
     */
    public ProcessResult executeScript(String interpreter, String scriptPath, String... args) {
        List<String> cmdList = new ArrayList<>();
        cmdList.add(interpreter);
        cmdList.add(scriptPath);
        for (String arg : args) {
            cmdList.add(arg);
        }

        String[] cmdArray = cmdList.toArray(new String[0]);
        File workDir = new File(scriptPath).getParentFile();

        return execute(cmdArray, null, workDir);
    }

    /**
     * 解析命令字符串为命令数组
     * @param command 命令字符串
     * @return 命令数组
     */
    private String[] parseCommand(String command) {
        List<String> result = new ArrayList<>();
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

        return result.toArray(new String[0]);
    }
}
