package com.github.catvod.spider.binrunner.terminal;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 命令解析器
 * 解析用户输入的命令行
 */
public class CommandParser {

    private static final Pattern QUOTED_STRING = Pattern.compile("\"([^\"]*)\"|'([^']*)'");
    
    /**
     * 解析结果
     */
    public static class ParsedCommand {
        private final String command;
        private final String[] args;
        private final boolean background;
        private final String redirectOut;
        private final String redirectIn;

        public ParsedCommand(String command, String[] args, boolean background, 
                           String redirectOut, String redirectIn) {
            this.command = command;
            this.args = args;
            this.background = background;
            this.redirectOut = redirectOut;
            this.redirectIn = redirectIn;
        }

        public String getCommand() {
            return command;
        }

        public String[] getArgs() {
            return args;
        }

        public boolean isBackground() {
            return background;
        }

        public String getRedirectOut() {
            return redirectOut;
        }

        public String getRedirectIn() {
            return redirectIn;
        }

        /**
         * 获取完整命令行
         */
        public String getFullCommand() {
            StringBuilder sb = new StringBuilder(command);
            for (String arg : args) {
                sb.append(" ");
                if (arg.contains(" ")) {
                    sb.append("\"").append(arg).append("\"");
                } else {
                    sb.append(arg);
                }
            }
            return sb.toString();
        }
    }

    /**
     * 解析命令行
     * @param input 用户输入
     * @return 解析结果
     */
    public static ParsedCommand parse(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }

        String line = input.trim();
        
        // 检查后台运行标记
        boolean background = line.endsWith("&");
        if (background) {
            line = line.substring(0, line.length() - 1).trim();
        }

        // 检查输出重定向
        String redirectOut = null;
        int redirectOutIndex = line.indexOf(">");
        if (redirectOutIndex > 0) {
            redirectOut = line.substring(redirectOutIndex + 1).trim();
            line = line.substring(0, redirectOutIndex).trim();
        }

        // 检查输入重定向
        String redirectIn = null;
        int redirectInIndex = line.indexOf("<");
        if (redirectInIndex > 0) {
            redirectIn = line.substring(redirectInIndex + 1).trim();
            line = line.substring(0, redirectInIndex).trim();
        }

        // 分割命令和参数
        List<String> tokens = tokenize(line);
        if (tokens.isEmpty()) {
            return null;
        }

        String command = tokens.get(0);
        String[] args = new String[tokens.size() - 1];
        for (int i = 1; i < tokens.size(); i++) {
            args[i - 1] = tokens.get(i);
        }

        return new ParsedCommand(command, args, background, redirectOut, redirectIn);
    }

    /**
     * 分词
     */
    private static List<String> tokenize(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuote = false;
        char quoteChar = 0;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

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
                } else if (Character.isWhitespace(c)) {
                    if (current.length() > 0) {
                        tokens.add(current.toString());
                        current = new StringBuilder();
                    }
                } else {
                    current.append(c);
                }
            }
        }

        if (current.length() > 0) {
            tokens.add(current.toString());
        }

        return tokens;
    }

    /**
     * 分割管道命令
     * @param input 命令行
     * @return 命令列表
     */
    public static List<String> splitPipe(String input) {
        List<String> commands = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuote = false;
        char quoteChar = 0;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (inQuote) {
                if (c == quoteChar) {
                    inQuote = false;
                }
                current.append(c);
            } else {
                if (c == '"' || c == '\'') {
                    inQuote = true;
                    quoteChar = c;
                    current.append(c);
                } else if (c == '|') {
                    if (current.length() > 0) {
                        commands.add(current.toString().trim());
                        current = new StringBuilder();
                    }
                } else {
                    current.append(c);
                }
            }
        }

        if (current.length() > 0) {
            commands.add(current.toString().trim());
        }

        return commands;
    }

    /**
     * 检查是否为内置命令
     */
    public static boolean isBuiltinCommand(String command) {
        return BuiltinCommands.isBuiltin(command);
    }

    /**
     * 替换环境变量
     */
    public static String expandVariables(String line, java.util.Map<String, String> env) {
        if (line == null || !line.contains("$")) {
            return line;
        }

        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < line.length()) {
            char c = line.charAt(i);
            if (c == '$' && i + 1 < line.length()) {
                // 提取变量名
                int start = i + 1;
                int end = start;
                
                if (line.charAt(start) == '{') {
                    // ${VAR} 格式
                    end = line.indexOf('}', start);
                    if (end > start) {
                        String varName = line.substring(start + 1, end);
                        String value = env.getOrDefault(varName, "");
                        result.append(value);
                        i = end + 1;
                        continue;
                    }
                } else {
                    // $VAR 格式
                    while (end < line.length()) {
                        char ch = line.charAt(end);
                        if (Character.isLetterOrDigit(ch) || ch == '_') {
                            end++;
                        } else {
                            break;
                        }
                    }
                    if (end > start) {
                        String varName = line.substring(start, end);
                        String value = env.getOrDefault(varName, "");
                        result.append(value);
                        i = end;
                        continue;
                    }
                }
            }
            result.append(c);
            i++;
        }

        return result.toString();
    }
}
