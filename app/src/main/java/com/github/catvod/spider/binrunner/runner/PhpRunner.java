package com.github.catvod.spider.binrunner.runner;

import com.github.catvod.spider.binrunner.core.EnvManager;
import com.github.catvod.spider.binrunner.util.EnvBuilder;

/**
 * PHP 执行器
 * 用于执行 PHP 脚本
 */
public class PhpRunner extends BaseRunner {

    /**
     * 创建 PHP Runner
     * @param envManager 环境管理器
     * @param spiderPath 爬虫脚本路径
     */
    public PhpRunner(EnvManager envManager, String spiderPath) {
        super(envManager, spiderPath);
    }

    @Override
    protected String getInterpreterName() {
        return "php";
    }

    @Override
    protected String getRunnerScriptName() {
        return "runner.php";
    }

    @Override
    protected void configureEnvironment(EnvBuilder envBuilder) {
        // 设置 PHP 配置目录
        envBuilder.setPhpIniDir(envManager.getPhpBaseDir().getParent());
        
        // 设置工作目录
        envBuilder.setWorkDir(envManager.getPrivateDir().getAbsolutePath());
        
        // 设置 HOME 目录
        envBuilder.setHome(envManager.getPrivateDir().getAbsolutePath());
    }
}
