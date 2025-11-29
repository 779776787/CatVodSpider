package com.github.catvod.spider.binrunner.runner;

import com.github.catvod.spider.binrunner.bridge.BinBridge;
import com.github.catvod.spider.binrunner.core.EnvManager;
import com.github.catvod.spider.binrunner.util.EnvBuilder;

/**
 * QuickJS 执行器
 * 用于执行 QuickJS 脚本
 */
public class JsRunner extends BaseRunner {

    private int bridgePort = -1;

    /**
     * 创建 QuickJS Runner
     * @param envManager 环境管理器
     * @param spiderPath 爬虫脚本路径
     */
    public JsRunner(EnvManager envManager, String spiderPath) {
        super(envManager, spiderPath);
        initBridge();
    }

    /**
     * 初始化桥接服务
     */
    private void initBridge() {
        BinBridge bridge = BinBridge.getInstance();
        if (!bridge.isRunning()) {
            bridgePort = bridge.start();
        } else {
            bridgePort = bridge.getPort();
        }
    }

    @Override
    protected String getInterpreterName() {
        return "qjs";
    }

    @Override
    protected String getRunnerScriptName() {
        return "runner.js";
    }

    @Override
    protected void configureEnvironment(EnvBuilder envBuilder) {
        // 设置 JS 库路径
        envBuilder.set("JS_LIB_PATH", envManager.getJsLibDir().getAbsolutePath());
        
        // 设置桥接端口
        if (bridgePort > 0) {
            envBuilder.setBridgePort(bridgePort);
        }
        
        // 设置工作目录
        envBuilder.setWorkDir(envManager.getPrivateDir().getAbsolutePath());
    }

    @Override
    protected String[] buildScriptArgs(String spiderPath, String method, Object... args) {
        // QuickJS 需要 --std 参数来启用标准库
        String[] baseArgs = super.buildScriptArgs(spiderPath, method, args);
        return baseArgs;
    }
}
