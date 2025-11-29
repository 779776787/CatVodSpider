package com.github.catvod.spider.binrunner.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 环境变量构建器
 * 用于构建进程执行时的环境变量
 */
public class EnvBuilder {

    private final Map<String, String> env;

    public EnvBuilder() {
        this.env = new HashMap<>();
        // 复制系统环境变量
        env.putAll(System.getenv());
    }

    /**
     * 设置环境变量
     */
    public EnvBuilder set(String key, String value) {
        if (key != null && value != null) {
            env.put(key, value);
        }
        return this;
    }

    /**
     * 设置 LD_LIBRARY_PATH
     * @param libDir 库目录路径
     */
    public EnvBuilder setLibPath(String libDir) {
        if (libDir != null && new File(libDir).exists()) {
            String existing = env.get("LD_LIBRARY_PATH");
            if (existing != null && !existing.isEmpty()) {
                env.put("LD_LIBRARY_PATH", libDir + ":" + existing);
            } else {
                env.put("LD_LIBRARY_PATH", libDir);
            }
        }
        return this;
    }

    /**
     * 设置 Python 环境变量
     * @param pythonPath Python 模块搜索路径
     */
    public EnvBuilder setPythonPath(String pythonPath) {
        if (pythonPath != null) {
            String existing = env.get("PYTHONPATH");
            if (existing != null && !existing.isEmpty()) {
                env.put("PYTHONPATH", pythonPath + ":" + existing);
            } else {
                env.put("PYTHONPATH", pythonPath);
            }
        }
        return this;
    }

    /**
     * 设置 Node.js 环境变量
     * @param nodePath Node.js 模块搜索路径
     */
    public EnvBuilder setNodePath(String nodePath) {
        if (nodePath != null) {
            String existing = env.get("NODE_PATH");
            if (existing != null && !existing.isEmpty()) {
                env.put("NODE_PATH", nodePath + ":" + existing);
            } else {
                env.put("NODE_PATH", nodePath);
            }
        }
        return this;
    }

    /**
     * 设置 PHP 环境变量
     * @param phpIniDir PHP 配置目录
     */
    public EnvBuilder setPhpIniDir(String phpIniDir) {
        if (phpIniDir != null) {
            env.put("PHPRC", phpIniDir);
        }
        return this;
    }

    /**
     * 设置 Lua 环境变量
     * @param luaPath Lua 模块搜索路径
     */
    public EnvBuilder setLuaPath(String luaPath) {
        if (luaPath != null) {
            String existing = env.get("LUA_PATH");
            if (existing != null && !existing.isEmpty()) {
                env.put("LUA_PATH", luaPath + ";" + existing);
            } else {
                env.put("LUA_PATH", luaPath + ";./?.lua;./?/init.lua");
            }
        }
        return this;
    }

    /**
     * 设置 HOME 目录
     */
    public EnvBuilder setHome(String home) {
        if (home != null) {
            env.put("HOME", home);
        }
        return this;
    }

    /**
     * 设置 TMPDIR 目录
     */
    public EnvBuilder setTmpDir(String tmpDir) {
        if (tmpDir != null) {
            env.put("TMPDIR", tmpDir);
        }
        return this;
    }

    /**
     * 设置 PATH 环境变量，添加新路径到开头
     */
    public EnvBuilder addPath(String path) {
        if (path != null && new File(path).exists()) {
            String existing = env.get("PATH");
            if (existing != null && !existing.isEmpty()) {
                env.put("PATH", path + ":" + existing);
            } else {
                env.put("PATH", path);
            }
        }
        return this;
    }

    /**
     * 设置桥接服务端口
     */
    public EnvBuilder setBridgePort(int port) {
        env.put("BINRUNNER_BRIDGE_PORT", String.valueOf(port));
        return this;
    }

    /**
     * 设置代理端口
     */
    public EnvBuilder setProxyPort(int port) {
        env.put("BINRUNNER_PROXY_PORT", String.valueOf(port));
        return this;
    }

    /**
     * 设置工作目录
     */
    public EnvBuilder setWorkDir(String workDir) {
        if (workDir != null) {
            env.put("BINRUNNER_WORK_DIR", workDir);
        }
        return this;
    }

    /**
     * 移除环境变量
     */
    public EnvBuilder remove(String key) {
        env.remove(key);
        return this;
    }

    /**
     * 获取环境变量值
     */
    public String get(String key) {
        return env.get(key);
    }

    /**
     * 获取环境变量映射
     */
    public Map<String, String> getEnv() {
        return new HashMap<>(env);
    }

    /**
     * 转换为字符串数组（用于 ProcessBuilder）
     */
    public String[] toArray() {
        String[] result = new String[env.size()];
        int i = 0;
        for (Map.Entry<String, String> entry : env.entrySet()) {
            result[i++] = entry.getKey() + "=" + entry.getValue();
        }
        return result;
    }

    /**
     * 合并其他环境变量
     */
    public EnvBuilder merge(Map<String, String> other) {
        if (other != null) {
            env.putAll(other);
        }
        return this;
    }

    /**
     * 清空环境变量（保留系统默认）
     */
    public EnvBuilder clear() {
        env.clear();
        env.putAll(System.getenv());
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : env.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }
}
