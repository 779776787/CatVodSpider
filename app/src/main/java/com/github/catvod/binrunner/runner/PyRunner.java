package com.github.catvod.binrunner.runner;

import com.github.catvod.binrunner.core.EnvManager;
import com.github.catvod.binrunner.util.BinLogger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Python 脚本执行器
 * 通过命令行调用 Python 解释器执行脚本
 */
public class PyRunner extends BaseRunner {

    private static final String RUNNER_TYPE = "python";

    /**
     * 构造函数
     * @param scriptPath 脚本文件路径
     * @param envManager 环境管理器
     */
    public PyRunner(String scriptPath, EnvManager envManager) {
        super(scriptPath, envManager);
        
        // 获取 Python 解释器路径
        if (envManager != null) {
            this.binaryPath = envManager.getPythonPath();
        }
        
        if (this.binaryPath == null || this.binaryPath.isEmpty()) {
            BinLogger.warn("Python 解释器未找到，请确保已安装 Python");
        }
    }

    @Override
    public void init(String extend) {
        BinLogger.info("初始化 Python Runner: " + scriptPath);
        String args = buildArgsJson("extend", extend != null ? extend : "");
        callMethod("init", args);
    }

    @Override
    public String homeContent(boolean filter) {
        String args = buildArgsJson("filter", filter);
        return callMethod("homeContent", args);
    }

    @Override
    public String homeVideoContent() {
        return callMethod("homeVideoContent", "");
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) {
        String args = buildArgsJson(
            "tid", tid,
            "pg", pg,
            "filter", filter,
            "extend", extend != null ? extend : new HashMap<>()
        );
        return callMethod("categoryContent", args);
    }

    @Override
    public String detailContent(List<String> ids) {
        String args = buildArgsJson("ids", ids);
        return callMethod("detailContent", args);
    }

    @Override
    public String searchContent(String key, boolean quick, String pg) {
        String args = buildArgsJson(
            "key", key,
            "quick", quick,
            "pg", pg
        );
        return callMethod("searchContent", args);
    }

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) {
        String args = buildArgsJson(
            "flag", flag,
            "id", id,
            "vipFlags", vipFlags
        );
        return callMethod("playerContent", args);
    }

    @Override
    public String liveContent(String url) {
        String args = buildArgsJson("url", url);
        return callMethod("liveContent", args);
    }

    @Override
    public String action(String action) {
        String args = buildArgsJson("action", action);
        return callMethod("action", args);
    }

    @Override
    public void destroy() {
        BinLogger.info("销毁 Python Runner: " + scriptPath);
        callMethod("destroy", "");
    }

    @Override
    protected Map<String, String> getEnvMap() {
        Map<String, String> envs = super.getEnvMap();
        
        // 添加 Python 特定的环境变量
        if (envManager != null) {
            envs.putAll(envManager.getEnvMapForType(RUNNER_TYPE));
        }
        
        // 设置 PYTHONIOENCODING 确保 UTF-8 输出
        envs.put("PYTHONIOENCODING", "utf-8");
        
        return envs;
    }
}
