package com.github.catvod.binrunner.terminal;

import com.github.catvod.bean.Class;
import com.github.catvod.bean.Result;
import com.github.catvod.bean.Vod;
import com.github.catvod.binrunner.core.BinExecutor;
import com.github.catvod.binrunner.core.EnvManager;
import com.github.catvod.binrunner.core.ProcessManager;
import com.github.catvod.binrunner.process.ProcessResult;
import com.github.catvod.binrunner.util.BinLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * 交互式终端
 * 提供命令行界面供用户执行命令
 */
public class Terminal {

    private static final String TERMINAL_TITLE = "BinRunner 终端";
    private static final String TERMINAL_PROMPT = "$ ";

    private final EnvManager envManager;
    private final ProcessManager processManager;
    private final BinExecutor executor;
    private final BuiltinCommands builtinCommands;
    private final List<String> outputHistory;
    private String lastOutput;

    /**
     * 构造函数
     * @param envManager 环境管理器
     * @param processManager 进程管理器
     */
    public Terminal(EnvManager envManager, ProcessManager processManager) {
        this.envManager = envManager;
        this.processManager = processManager;
        this.executor = new BinExecutor(envManager);
        this.builtinCommands = new BuiltinCommands(envManager, processManager);
        this.outputHistory = new ArrayList<>();
        this.lastOutput = "";
    }

    /**
     * 获取首页内容
     * @return 首页内容 JSON
     */
    public String getHomeContent() {
        List<Class> classes = new ArrayList<>();
        classes.add(new Class("terminal", "终端"));
        classes.add(new Class("help", "帮助"));
        classes.add(new Class("history", "历史"));

        List<Vod> list = new ArrayList<>();
        
        // 添加终端入口
        Vod terminal = new Vod();
        terminal.setVodId("terminal");
        terminal.setVodName(TERMINAL_TITLE);
        terminal.setVodRemarks("交互式命令行");
        list.add(terminal);

        // 添加帮助入口
        Vod help = new Vod();
        help.setVodId("help");
        help.setVodName("帮助文档");
        help.setVodRemarks("查看使用说明");
        list.add(help);

        return Result.string(classes, list);
    }

    /**
     * 处理用户输入
     * @param input 用户输入
     * @return 处理结果
     */
    public String handleInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            return formatOutput("请输入命令");
        }

        BinLogger.command(input);

        // 解析命令
        CommandParser.ParseResult parsed = CommandParser.parse(input);
        if (parsed.isEmpty()) {
            return formatOutput("空命令");
        }

        String command = parsed.getCommand();
        List<String> args = parsed.getArgs();

        // 检查是否为内置命令
        if (CommandParser.isBuiltinCommand(command)) {
            String result = builtinCommands.execute(command, args);
            return formatOutput(result);
        }

        // 执行外部命令
        ProcessResult result = executor.execute(parsed.getFullCommand());
        
        if (result.isSuccess()) {
            lastOutput = result.getStdout();
        } else {
            lastOutput = result.getAllOutput();
        }

        addToHistory(input, lastOutput);

        return formatOutput(lastOutput);
    }

    /**
     * 格式化输出
     * @param output 输出内容
     * @return 格式化的结果
     */
    private String formatOutput(String output) {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append(TERMINAL_TITLE).append("\n");
        sb.append("========================================\n\n");
        
        if (output != null && !output.isEmpty()) {
            sb.append(output);
            if (!output.endsWith("\n")) {
                sb.append("\n");
            }
        }
        
        sb.append("\n").append(TERMINAL_PROMPT);

        // 返回 action 结果
        return Result.get().msg(sb.toString()).string();
    }

    /**
     * 添加到历史记录
     * @param command 命令
     * @param output 输出
     */
    private void addToHistory(String command, String output) {
        String entry = "[" + getCurrentTime() + "] " + command + "\n" + output;
        outputHistory.add(entry);
        
        // 限制历史记录数量
        while (outputHistory.size() > 100) {
            outputHistory.remove(0);
        }
    }

    /**
     * 获取历史记录
     * @return 历史记录列表
     */
    public List<String> getHistory() {
        return new ArrayList<>(outputHistory);
    }

    /**
     * 获取最后输出
     * @return 最后输出
     */
    public String getLastOutput() {
        return lastOutput;
    }

    /**
     * 清除历史记录
     */
    public void clearHistory() {
        outputHistory.clear();
    }

    /**
     * 获取当前时间字符串
     * @return 时间字符串
     */
    private String getCurrentTime() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date());
    }
}
