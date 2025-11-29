package com.github.catvod.binrunner.runner;

import com.github.catvod.binrunner.core.BinExecutor;
import com.github.catvod.binrunner.core.EnvManager;
import com.github.catvod.binrunner.process.ProcessResult;
import com.github.catvod.binrunner.util.BinLogger;
import com.google.gson.Gson;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 脚本执行器基类
 * 所有语言的 Runner 都继承此类
 */
public abstract class BaseRunner {

    protected String scriptPath;      // 脚本文件路径
    protected String binaryPath;      // 解释器路径
    protected String libPath;         // 库路径
    protected EnvManager envManager;
    protected BinExecutor executor;
    protected Gson gson;

    /**
     * 构造函数
     * @param scriptPath 脚本文件路径
     * @param envManager 环境管理器
     */
    public BaseRunner(String scriptPath, EnvManager envManager) {
        this.scriptPath = scriptPath;
        this.envManager = envManager;
        this.executor = new BinExecutor(envManager);
        this.gson = new Gson();
    }

    /**
     * 初始化脚本
     * @param extend 扩展参数
     */
    public abstract void init(String extend);

    /**
     * 获取首页内容
     * @param filter 是否包含筛选
     * @return 首页内容 JSON
     */
    public abstract String homeContent(boolean filter);

    /**
     * 获取首页视频
     * @return 首页视频 JSON
     */
    public abstract String homeVideoContent();

    /**
     * 获取分类内容
     * @param tid 分类ID
     * @param pg 页码
     * @param filter 是否包含筛选
     * @param extend 扩展参数
     * @return 分类内容 JSON
     */
    public abstract String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend);

    /**
     * 获取详情
     * @param ids ID列表
     * @return 详情 JSON
     */
    public abstract String detailContent(List<String> ids);

    /**
     * 搜索内容
     * @param key 关键词
     * @param quick 是否快速搜索
     * @param pg 页码
     * @return 搜索结果 JSON
     */
    public abstract String searchContent(String key, boolean quick, String pg);

    /**
     * 获取播放内容
     * @param flag 播放标识
     * @param id 视频ID
     * @param vipFlags VIP标识列表
     * @return 播放内容 JSON
     */
    public abstract String playerContent(String flag, String id, List<String> vipFlags);

    /**
     * 直播内容
     * @param url 直播URL
     * @return 直播内容 JSON
     */
    public abstract String liveContent(String url);

    /**
     * 执行动作
     * @param action 动作字符串
     * @return 执行结果
     */
    public abstract String action(String action);

    /**
     * 销毁
     */
    public abstract void destroy();

    /**
     * 执行脚本方法
     * @param method 方法名
     * @param args 参数（JSON格式）
     * @return 执行结果（JSON字符串）
     */
    protected String callMethod(String method, String args) {
        if (binaryPath == null || binaryPath.isEmpty()) {
            BinLogger.error("解释器路径未设置");
            return "";
        }

        if (scriptPath == null || scriptPath.isEmpty()) {
            BinLogger.error("脚本路径未设置");
            return "";
        }

        File scriptFile = new File(scriptPath);
        if (!scriptFile.exists()) {
            BinLogger.error("脚本文件不存在: " + scriptPath);
            return "";
        }

        // 构建命令
        String[] cmdArray;
        if (args != null && !args.isEmpty()) {
            cmdArray = new String[]{binaryPath, scriptPath, method, args};
        } else {
            cmdArray = new String[]{binaryPath, scriptPath, method};
        }

        // 获取环境变量
        Map<String, String> envs = getEnvMap();

        // 执行命令
        ProcessResult result = executor.execute(cmdArray, envs, scriptFile.getParentFile());

        if (result.isSuccess()) {
            return result.getStdout().trim();
        } else {
            BinLogger.error("脚本执行失败: " + result.getStderr());
            return "";
        }
    }

    /**
     * 获取环境变量映射
     * 子类可以覆盖此方法添加特定的环境变量
     * @return 环境变量映射
     */
    protected Map<String, String> getEnvMap() {
        return envManager != null ? envManager.getEnvMap() : new HashMap<>();
    }

    /**
     * 构建参数 JSON
     * @param params 参数键值对
     * @return JSON 字符串
     */
    protected String buildArgsJson(Object... params) {
        Map<String, Object> args = new HashMap<>();
        for (int i = 0; i < params.length - 1; i += 2) {
            args.put(String.valueOf(params[i]), params[i + 1]);
        }
        return gson.toJson(args);
    }

    /**
     * 获取脚本路径
     * @return 脚本路径
     */
    public String getScriptPath() {
        return scriptPath;
    }

    /**
     * 获取解释器路径
     * @return 解释器路径
     */
    public String getBinaryPath() {
        return binaryPath;
    }

    /**
     * 设置解释器路径
     * @param binaryPath 解释器路径
     */
    public void setBinaryPath(String binaryPath) {
        this.binaryPath = binaryPath;
    }
}
