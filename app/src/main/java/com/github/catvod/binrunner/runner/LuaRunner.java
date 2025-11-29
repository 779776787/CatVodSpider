package com.github.catvod.binrunner.runner;

import com.github.catvod.binrunner.core.EnvManager;
import com.github.catvod.binrunner.util.BinLogger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lua 脚本执行器
 * 通过命令行调用 Lua 解释器执行脚本
 */
public class LuaRunner extends BaseRunner {

    private static final String RUNNER_TYPE = "lua";

    /**
     * 构造函数
     * @param scriptPath 脚本文件路径
     * @param envManager 环境管理器
     */
    public LuaRunner(String scriptPath, EnvManager envManager) {
        super(scriptPath, envManager);
        
        // 获取 Lua 解释器路径
        if (envManager != null) {
            this.binaryPath = envManager.getLuaPath();
        }
        
        if (this.binaryPath == null || this.binaryPath.isEmpty()) {
            BinLogger.warn("Lua 解释器未找到，请确保已安装 Lua");
        }
    }

    @Override
    public void init(String extend) {
        BinLogger.info("初始化 Lua Runner: " + scriptPath);
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
        BinLogger.info("销毁 Lua Runner: " + scriptPath);
        callMethod("destroy", "");
    }

    @Override
    protected Map<String, String> getEnvMap() {
        Map<String, String> envs = super.getEnvMap();
        
        // 添加 Lua 特定的环境变量
        if (envManager != null) {
            envs.putAll(envManager.getEnvMapForType(RUNNER_TYPE));
        }
        
        return envs;
    }
}
