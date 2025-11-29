package com.github.catvod.binrunner.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 环境变量构建器
 * 用于构建进程执行时的环境变量
 */
public class EnvBuilder {

    private final Map<String, String> envMap;

    public EnvBuilder() {
        this.envMap = new HashMap<>();
    }

    /**
     * 添加环境变量
     * @param key 变量名
     * @param value 变量值
     * @return 当前构建器
     */
    public EnvBuilder put(String key, String value) {
        if (key != null && value != null) {
            envMap.put(key, value);
        }
        return this;
    }

    /**
     * 添加 PATH 环境变量
     * @param path PATH 路径
     * @return 当前构建器
     */
    public EnvBuilder addPath(String path) {
        String currentPath = envMap.getOrDefault("PATH", System.getenv("PATH"));
        if (currentPath == null || currentPath.isEmpty()) {
            envMap.put("PATH", path);
        } else {
            envMap.put("PATH", path + ":" + currentPath);
        }
        return this;
    }

    /**
     * 添加 LD_LIBRARY_PATH 环境变量
     * @param libPath 库路径
     * @return 当前构建器
     */
    public EnvBuilder addLibPath(String libPath) {
        String currentLibPath = envMap.getOrDefault("LD_LIBRARY_PATH", System.getenv("LD_LIBRARY_PATH"));
        if (currentLibPath == null || currentLibPath.isEmpty()) {
            envMap.put("LD_LIBRARY_PATH", libPath);
        } else {
            envMap.put("LD_LIBRARY_PATH", libPath + ":" + currentLibPath);
        }
        return this;
    }

    /**
     * 添加 HOME 环境变量
     * @param home HOME 路径
     * @return 当前构建器
     */
    public EnvBuilder setHome(String home) {
        envMap.put("HOME", home);
        return this;
    }

    /**
     * 添加 TMPDIR 环境变量
     * @param tmpDir 临时目录路径
     * @return 当前构建器
     */
    public EnvBuilder setTmpDir(String tmpDir) {
        envMap.put("TMPDIR", tmpDir);
        return this;
    }

    /**
     * 合并其他环境变量
     * @param other 其他环境变量映射
     * @return 当前构建器
     */
    public EnvBuilder merge(Map<String, String> other) {
        if (other != null) {
            envMap.putAll(other);
        }
        return this;
    }

    /**
     * 构建环境变量数组
     * @return 环境变量数组（格式: KEY=VALUE）
     */
    public String[] build() {
        String[] result = new String[envMap.size()];
        int index = 0;
        for (Map.Entry<String, String> entry : envMap.entrySet()) {
            result[index++] = entry.getKey() + "=" + entry.getValue();
        }
        return result;
    }

    /**
     * 获取环境变量映射
     * @return 环境变量映射
     */
    public Map<String, String> getEnvMap() {
        return new HashMap<>(envMap);
    }

    /**
     * 清空环境变量
     * @return 当前构建器
     */
    public EnvBuilder clear() {
        envMap.clear();
        return this;
    }

    /**
     * 从系统环境继承
     * @return 当前构建器
     */
    public EnvBuilder inheritSystemEnv() {
        envMap.putAll(System.getenv());
        return this;
    }
}
