package com.github.catvod.spider.binrunner.runner;

import com.github.catvod.spider.binrunner.core.EnvManager;
import com.github.catvod.spider.binrunner.util.EnvBuilder;

/**
 * Node.js 执行器
 * 用于执行 Node.js 脚本
 */
public class NodeRunner extends BaseRunner {

    /**
     * 创建 Node.js Runner
     * @param envManager 环境管理器
     * @param spiderPath 爬虫脚本路径
     */
    public NodeRunner(EnvManager envManager, String spiderPath) {
        super(envManager, spiderPath);
    }

    @Override
    protected String getInterpreterName() {
        return "node";
    }

    @Override
    protected String getRunnerScriptName() {
        return "runner_node.js";
    }

    @Override
    protected void configureEnvironment(EnvBuilder envBuilder) {
        // 设置 Node.js 模块路径
        envBuilder.setNodePath(envManager.getJsLibDir().getAbsolutePath());
        
        // 设置工作目录
        envBuilder.setWorkDir(envManager.getPrivateDir().getAbsolutePath());
        
        // 设置 HOME 目录
        envBuilder.setHome(envManager.getPrivateDir().getAbsolutePath());
    }
}
