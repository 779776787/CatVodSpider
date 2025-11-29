package com.github.catvod.spider.binrunner.runner;

import com.github.catvod.spider.binrunner.core.EnvManager;
import com.github.catvod.spider.binrunner.util.EnvBuilder;

/**
 * Lua 执行器
 * 用于执行 Lua 脚本
 */
public class LuaRunner extends BaseRunner {

    /**
     * 创建 Lua Runner
     * @param envManager 环境管理器
     * @param spiderPath 爬虫脚本路径
     */
    public LuaRunner(EnvManager envManager, String spiderPath) {
        super(envManager, spiderPath);
    }

    @Override
    protected String getInterpreterName() {
        return "lua";
    }

    @Override
    protected String getRunnerScriptName() {
        return "runner.lua";
    }

    @Override
    protected void configureEnvironment(EnvBuilder envBuilder) {
        // 设置 Lua 模块路径
        String luaPath = envManager.getLuaBaseDir().getAbsolutePath() + "/?.lua;" +
                         envManager.getLuaBaseDir().getAbsolutePath() + "/?/init.lua";
        envBuilder.setLuaPath(luaPath);
        
        // 设置工作目录
        envBuilder.setWorkDir(envManager.getPrivateDir().getAbsolutePath());
        
        // 设置 HOME 目录
        envBuilder.setHome(envManager.getPrivateDir().getAbsolutePath());
    }
}
