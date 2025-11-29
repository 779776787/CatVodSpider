package com.github.catvod.binrunner.terminal;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 命令解析器
 * 解析用户输入的命令字符串
 */
public class CommandParser {

    /**
     * 解析结果
     */
    public static class ParseResult {
        private final String command;
        private final List<String> args;
        private final boolean background;
        private final String pipeTarget;

        public ParseResult(String command, List<String> args, boolean background, String pipeTarget) {
            this.command = command;
            this.args = args;
            this.background = background;
            this.pipeTarget = pipeTarget;
        }

        /**
         * 获取命令名称
         * @return 命令名称
         */
        public String getCommand() {
            return command;
        }

        /**
         * 获取参数列表
         * @return 参数列表
         */
        public List<String> getArgs() {
            return args;
        }

        /**
         * 是否后台运行
         * @return 是否后台运行
         */
        public boolean isBackground() {
            return background;
        }

        /**
         * 获取管道目标
         * @return 管道目标命令，无管道返回 null
         */
        public String getPipeTarget() {
            return pipeTarget;
        }

        /**
         * 获取完整命令字符串
         * @return 完整命令
         */
        public String getFullCommand() {
            StringBuilder sb = new StringBuilder();
            sb.append(command);
            for (String arg : args) {
                sb.append(" ");
                // 如果参数包含空格，用引号包裹
                if (arg.contains(" ")) {
                    sb.append("\"").append(arg).append("\"");
                } else {
                    sb.append(arg);
                }
            }
            return sb.toString();
        }

        /**
         * 判断是否为空命令
         * @return 是否为空
         */
        public boolean isEmpty() {
            return command == null || command.isEmpty();
        }
    }

    /**
     * 解析命令字符串
     * @param input 输入字符串
     * @return 解析结果
     */
    public static ParseResult parse(String input) {
        if (input == null || input.trim().isEmpty()) {
            return new ParseResult("", new ArrayList<>(), false, null);
        }

        String trimmed = input.trim();
        
        // 检查是否后台运行
        boolean background = trimmed.endsWith("&");
        if (background) {
            trimmed = trimmed.substring(0, trimmed.length() - 1).trim();
        }

        // 检查管道
        String pipeTarget = null;
        int pipeIndex = findPipeIndex(trimmed);
        if (pipeIndex > 0) {
            pipeTarget = trimmed.substring(pipeIndex + 1).trim();
            trimmed = trimmed.substring(0, pipeIndex).trim();
        }

        // 解析命令和参数
        List<String> tokens = tokenize(trimmed);
        if (tokens.isEmpty()) {
            return new ParseResult("", new ArrayList<>(), background, pipeTarget);
        }

        String command = tokens.get(0);
        List<String> args = tokens.size() > 1 ? tokens.subList(1, tokens.size()) : new ArrayList<>();

        return new ParseResult(command, new ArrayList<>(args), background, pipeTarget);
    }

    /**
     * 分词
     * @param input 输入字符串
     * @return 分词结果
     */
    private static List<String> tokenize(String input) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        boolean escaped = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (escaped) {
                current.append(c);
                escaped = false;
                continue;
            }

            if (c == '\\') {
                escaped = true;
                continue;
            }

            if (c == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote;
                continue;
            }

            if (c == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
                continue;
            }

            if (Character.isWhitespace(c) && !inSingleQuote && !inDoubleQuote) {
                if (current.length() > 0) {
                    tokens.add(current.toString());
                    current = new StringBuilder();
                }
                continue;
            }

            current.append(c);
        }

        if (current.length() > 0) {
            tokens.add(current.toString());
        }

        return tokens;
    }

    /**
     * 查找管道符位置（忽略引号内的管道符）
     * @param input 输入字符串
     * @return 管道符位置，未找到返回 -1
     */
    private static int findPipeIndex(String input) {
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote;
            } else if (c == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
            } else if (c == '|' && !inSingleQuote && !inDoubleQuote) {
                return i;
            }
        }

        return -1;
    }

    /**
     * 判断是否为内置命令
     * @param command 命令名称
     * @return 是否为内置命令
     */
    public static boolean isBuiltinCommand(String command) {
        if (command == null || command.isEmpty()) {
            return false;
        }
        switch (command.toLowerCase()) {
            case "help":
            case "exit":
            case "clear":
            case "env":
            case "cd":
            case "pwd":
            case "history":
            case "alias":
            case "unalias":
            case "export":
            case "source":
            case "echo":
                return true;
            default:
                return false;
        }
    }
}
