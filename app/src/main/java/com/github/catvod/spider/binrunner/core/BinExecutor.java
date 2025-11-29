package com.github.catvod.spider.binrunner.core;

import com.github.catvod.spider.binrunner.config.AutoStartConfig;
import com.github.catvod.spider.binrunner.process.BinProcess;
import com.github.catvod.spider.binrunner.process.ProcessResult;
import com.github.catvod.spider.binrunner.process.ProcessState;
import com.github.catvod.spider.binrunner.util.BinLogger;
import com.github.catvod.spider.binrunner.util.EnvBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 二进制执行器
 * 负责执行二进制文件，包括：
 * 1. 构建进程和命令
 * 2. 设置环境变量
 * 3. 执行并捕获输出
 * 4. 处理超时和错误
 */
public class BinExecutor {

    private final EnvManager envManager;
    private int defaultTimeout = 60; // 秒

    /**
     * 创建执行器
     * @param envManager 环境管理器
     */
    public BinExecutor(EnvManager envManager) {
        this.envManager = envManager;
        if (envManager.getConfig() != null) {
            this.defaultTimeout = envManager.getConfig().getSettings().getTimeout();
        }
    }

    /**
     * 执行命令
     * @param command 完整命令
     * @param env 环境变量
     * @param timeout 超时时间（秒）
     * @return 执行结果
     */
    public ProcessResult execute(String command, Map<String, String> env, int timeout) {
        return execute(parseCommand(command), env, timeout);
    }

    /**
     * 执行命令
     * @param commands 命令数组
     * @param env 环境变量
     * @param timeout 超时时间（秒）
     * @return 执行结果
     */
    public ProcessResult execute(String[] commands, Map<String, String> env, int timeout) {
        BinProcess binProcess = new BinProcess("exec", String.join(" ", commands), false);
        return executeProcess(binProcess, commands, env, timeout);
    }

    /**
     * 执行进程
     */
    private ProcessResult executeProcess(BinProcess binProcess, String[] commands, Map<String, String> env, int timeout) {
        long startTime = System.currentTimeMillis();
        
        try {
            ProcessBuilder pb = new ProcessBuilder(commands);
            pb.redirectErrorStream(true);
            
            // 设置工作目录
            pb.directory(envManager.getPrivateDir());
            
            // 设置环境变量
            if (env != null) {
                pb.environment().putAll(env);
            }

            BinLogger.d("执行命令: " + String.join(" ", commands));
            
            Process process = pb.start();
            binProcess.setProcess(process);
            binProcess.setState(ProcessState.RUNNING);

            // 读取输出
            StringBuilder output = new StringBuilder();
            StringBuilder error = new StringBuilder();
            
            Thread outputThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                } catch (IOException e) {
                    // 忽略
                }
            });
            
            Thread errorThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        error.append(line).append("\n");
                    }
                } catch (IOException e) {
                    // 忽略
                }
            });

            outputThread.start();
            errorThread.start();

            // 等待进程完成
            boolean completed = process.waitFor(timeout > 0 ? timeout : defaultTimeout, TimeUnit.SECONDS);
            
            if (!completed) {
                process.destroyForcibly();
                binProcess.setState(ProcessState.TIMEOUT);
                BinLogger.w("命令执行超时: " + String.join(" ", commands));
                return ProcessResult.timeout("命令执行超时（" + timeout + "秒）");
            }

            // 等待输出线程完成
            outputThread.join(1000);
            errorThread.join(1000);

            int exitCode = process.exitValue();
            long executionTime = System.currentTimeMillis() - startTime;

            String stdout = output.toString().trim();
            String stderr = error.toString().trim();
            
            BinLogger.cmd(String.join(" ", commands), stdout.isEmpty() ? stderr : stdout);

            if (exitCode == 0) {
                binProcess.setState(ProcessState.COMPLETED);
                return ProcessResult.success(stdout, executionTime);
            } else {
                binProcess.setState(ProcessState.FAILED);
                return new ProcessResult.Builder()
                        .exitCode(exitCode)
                        .stdout(stdout)
                        .stderr(stderr)
                        .state(ProcessState.FAILED)
                        .executionTime(executionTime)
                        .build();
            }

        } catch (Exception e) {
            binProcess.setState(ProcessState.FAILED);
            BinLogger.e("执行命令失败: " + String.join(" ", commands), e);
            return ProcessResult.failure(e.getMessage(), -1);
        }
    }

    /**
     * 执行解释器脚本
     * @param interpreter 解释器名称（如 python3, qjs）
     * @param scriptPath 脚本路径
     * @param args 参数列表
     * @param env 额外环境变量
     * @return 执行结果
     */
    public ProcessResult executeScript(String interpreter, String scriptPath, String[] args, Map<String, String> env) {
        // 获取解释器路径
        File interpreterFile = envManager.getInterpreter(interpreter);
        if (interpreterFile == null) {
            return ProcessResult.failure("找不到解释器: " + interpreter, -1);
        }

        // 构建命令
        List<String> commandList = new ArrayList<>();
        commandList.add(interpreterFile.getAbsolutePath());
        
        // 添加解释器特定选项
        if (interpreter.contains("qjs") || interpreter.contains("quickjs")) {
            commandList.add("--std");
        }
        
        commandList.add(scriptPath);
        
        if (args != null) {
            for (String arg : args) {
                commandList.add(arg);
            }
        }

        // 构建环境变量
        EnvBuilder envBuilder = new EnvBuilder();
        
        // 设置 LD_LIBRARY_PATH
        File libDir = envManager.getLibDir(interpreter);
        if (libDir != null) {
            envBuilder.setLibPath(libDir.getAbsolutePath());
        }

        // 根据解释器类型设置特定环境变量
        setupInterpreterEnv(envBuilder, interpreter);

        // 合并额外环境变量
        envBuilder.merge(env);

        return execute(commandList.toArray(new String[0]), envBuilder.getEnv(), defaultTimeout);
    }

    /**
     * 设置解释器特定的环境变量
     */
    private void setupInterpreterEnv(EnvBuilder envBuilder, String interpreter) {
        String realName = envManager.getConfig().resolveAlias(interpreter);
        
        if (realName.contains("python")) {
            // Python 环境变量
            envBuilder.setPythonPath(envManager.getPythonBaseDir().getParentFile().getAbsolutePath());
            envBuilder.set("PYTHONIOENCODING", "utf-8");
        } else if (realName.contains("node")) {
            // Node.js 环境变量
            envBuilder.setNodePath(envManager.getJsLibDir().getAbsolutePath());
        } else if (realName.contains("php")) {
            // PHP 环境变量
            envBuilder.setPhpIniDir(envManager.getPhpBaseDir().getParent());
        } else if (realName.contains("lua")) {
            // Lua 环境变量
            envBuilder.setLuaPath(envManager.getLuaBaseDir().getAbsolutePath() + "/?.lua");
        }
    }

    /**
     * 执行后台进程
     * @param process 进程对象
     * @param config 自动启动配置
     */
    public void executeBackground(BinProcess process, AutoStartConfig config) {
        try {
            String[] commands = parseCommand(config.getCmd());
            
            // 检查并替换别名
            if (commands.length > 0) {
                File interpreterFile = envManager.getInterpreter(commands[0]);
                if (interpreterFile != null) {
                    commands[0] = interpreterFile.getAbsolutePath();
                }
            }

            ProcessBuilder pb = new ProcessBuilder(commands);
            pb.redirectErrorStream(true);
            pb.directory(envManager.getPrivateDir());

            // 设置环境变量
            EnvBuilder envBuilder = new EnvBuilder();
            if (commands.length > 0) {
                setupInterpreterEnv(envBuilder, commands[0]);
            }
            pb.environment().putAll(envBuilder.getEnv());

            BinLogger.i("启动后台进程: " + config.getName() + " -> " + config.getCmd());
            
            Process nativeProcess = pb.start();
            process.setProcess(nativeProcess);
            process.setState(ProcessState.RUNNING);

            // 后台读取输出（可选日志记录）
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(nativeProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        BinLogger.d("[" + config.getName() + "] " + line);
                    }
                } catch (IOException e) {
                    // 进程结束时会抛出异常，忽略
                }
            }).start();

        } catch (Exception e) {
            process.setState(ProcessState.FAILED);
            BinLogger.e("启动后台进程失败: " + config.getName(), e);
        }
    }

    /**
     * 解析命令字符串为数组
     */
    private String[] parseCommand(String command) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuote = false;
        char quoteChar = 0;

        for (int i = 0; i < command.length(); i++) {
            char c = command.charAt(i);
            
            if (inQuote) {
                if (c == quoteChar) {
                    inQuote = false;
                } else {
                    current.append(c);
                }
            } else {
                if (c == '"' || c == '\'') {
                    inQuote = true;
                    quoteChar = c;
                } else if (c == ' ' || c == '\t') {
                    if (current.length() > 0) {
                        parts.add(current.toString());
                        current = new StringBuilder();
                    }
                } else {
                    current.append(c);
                }
            }
        }
        
        if (current.length() > 0) {
            parts.add(current.toString());
        }

        return parts.toArray(new String[0]);
    }

    /**
     * 设置默认超时时间
     */
    public void setDefaultTimeout(int timeout) {
        this.defaultTimeout = timeout;
    }

    /**
     * 获取默认超时时间
     */
    public int getDefaultTimeout() {
        return defaultTimeout;
    }
}
