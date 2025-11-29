package com.github.catvod.spider.binrunner.runner;

import com.github.catvod.spider.binrunner.core.EnvManager;
import com.github.catvod.spider.binrunner.util.EnvBuilder;

/**
 * Python 执行器
 * 用于执行 Python 脚本
 */
public class PyRunner extends BaseRunner {

    /**
     * 创建 Python Runner
     * @param envManager 环境管理器
     * @param spiderPath 爬虫脚本路径
     */
    public PyRunner(EnvManager envManager, String spiderPath) {
        super(envManager, spiderPath);
    }

    @Override
    protected String getInterpreterName() {
        return "python3";
    }

    @Override
    protected String getRunnerScriptName() {
        return "runner.py";
    }

    @Override
    protected void configureEnvironment(EnvBuilder envBuilder) {
        // 设置 Python 模块路径（让爬虫能 import base.spider）
        envBuilder.setPythonPath(envManager.getPythonBaseDir().getParentFile().getAbsolutePath());
        
        // 设置 Python IO 编码
        envBuilder.set("PYTHONIOENCODING", "utf-8");
        envBuilder.set("PYTHONUNBUFFERED", "1");
        
        // 设置工作目录
        envBuilder.setWorkDir(envManager.getPrivateDir().getAbsolutePath());
        
        // 设置 HOME 目录
        envBuilder.setHome(envManager.getPrivateDir().getAbsolutePath());
    }
}
