package com.github.catvod.binrunner.terminal;

import java.util.ArrayList;
import java.util.List;

/**
 * Command parser for BinRunner terminal.
 * Handles parsing of user input into command and arguments.
 */
public class CommandParser {

    /**
     * Parsed command result.
     */
    public static class ParsedCommand {
        public final String command;
        public final List<String> args;
        public final boolean background;
        public final String rawInput;

        public ParsedCommand(String command, List<String> args, boolean background, String rawInput) {
            this.command = command;
            this.args = args;
            this.background = background;
            this.rawInput = rawInput;
        }

        /**
         * Get full command string (command + args).
         *
         * @return full command string
         */
        public String getFullCommand() {
            if (args.isEmpty()) {
                return command;
            }
            StringBuilder sb = new StringBuilder(command);
            for (String arg : args) {
                sb.append(" ").append(arg);
            }
            return sb.toString();
        }

        /**
         * Get first argument if exists.
         *
         * @return first argument or empty string
         */
        public String getFirstArg() {
            return args.isEmpty() ? "" : args.get(0);
        }

        /**
         * Check if command is empty.
         *
         * @return true if empty
         */
        public boolean isEmpty() {
            return command == null || command.isEmpty();
        }
    }

    /**
     * Parse input string into ParsedCommand.
     *
     * @param input user input string
     * @return parsed command
     */
    public static ParsedCommand parse(String input) {
        if (input == null || input.trim().isEmpty()) {
            return new ParsedCommand("", new ArrayList<>(), false, input);
        }

        String trimmed = input.trim();
        
        // Check for background flag
        boolean background = trimmed.endsWith("&");
        if (background) {
            trimmed = trimmed.substring(0, trimmed.length() - 1).trim();
        }

        // Parse tokens
        List<String> tokens = tokenize(trimmed);
        
        if (tokens.isEmpty()) {
            return new ParsedCommand("", new ArrayList<>(), background, input);
        }

        String command = tokens.get(0);
        List<String> args = tokens.subList(1, tokens.size());

        return new ParsedCommand(command, new ArrayList<>(args), background, input);
    }

    /**
     * Tokenize input string respecting quotes.
     *
     * @param input input string
     * @return list of tokens
     */
    private static List<String> tokenize(String input) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        char quoteChar = 0;
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
     * Check if command is a builtin command.
     *
     * @param command command name
     * @return true if builtin
     */
    public static boolean isBuiltin(String command) {
        switch (command.toLowerCase()) {
            case "ps":
            case "kill":
            case "bg":
            case "fg":
            case "env":
            case "which":
            case "clear":
            case "help":
            case "exit":
            case "quit":
                return true;
            default:
                return false;
        }
    }

    /**
     * Quote a string for shell if needed.
     *
     * @param s string to quote
     * @return quoted string
     */
    public static String quote(String s) {
        if (s == null) {
            return "";
        }
        if (!s.contains(" ") && !s.contains("\"") && !s.contains("'")) {
            return s;
        }
        return "\"" + s.replace("\"", "\\\"") + "\"";
    }
}
