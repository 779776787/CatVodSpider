package com.github.catvod.spider;

import android.content.Context;

import com.github.catvod.crawler.Spider;
import com.github.catvod.spider.binrunner.bridge.BinBridge;
import com.github.catvod.spider.binrunner.core.EnvManager;
import com.github.catvod.spider.binrunner.core.ProcessManager;
import com.github.catvod.spider.binrunner.runner.BaseRunner;
import com.github.catvod.spider.binrunner.runner.RunnerFactory;
import com.github.catvod.spider.binrunner.util.BinLogger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BinRunner 爬虫引擎
 * 用于在 Android TV 上执行 Termux 提取的二进制文件（Python3、QuickJS、PHP、Lua、Node.js 等）
 * 
 * 支持的脚本类型：
 * - .js  - QuickJS/JavaScript
 * - .py  - Python
 * - .php - PHP
 * - .lua - Lua
 * 
 * 使用方式：
 * 在配置中设置 ext 为脚本路径，如：
 * ext = "/storage/emulated/0/TV/spiders/my_spider.py"
 */
public class BinRunner extends Spider {

    private EnvManager envManager;
    private ProcessManager processManager;
    private BaseRunner runner;
    private String spiderPath;
    private boolean initialized = false;

    @Override
    public void init(Context context, String extend) throws Exception {
        super.init(context, extend);
        
        BinLogger.i("BinRunner 初始化开始");
        
        // 保存脚本路径
        this.spiderPath = extend;
        
        // 检查脚本类型是否支持
        if (!RunnerFactory.isSupported(spiderPath)) {
            BinLogger.e("不支持的脚本类型: " + spiderPath);
            throw new Exception("不支持的脚本类型: " + getFileExtension(spiderPath));
        }

        // 初始化环境管理器
        envManager = EnvManager.getInstance();
        if (!envManager.isReady()) {
            envManager.init(context);
        }

        // 检查环境是否就绪
        if (!envManager.isReady()) {
            BinLogger.w("环境未就绪，可能缺少解释器");
        }

        // 初始化进程管理器
        processManager = ProcessManager.getInstance();
        if (!processManager.isInitialized()) {
            processManager.init(envManager.getConfig().getSettings().getMaxProcesses());
            
            // 启动自动启动进程
            processManager.startAutoStartProcesses(
                    envManager.getConfig().getSettings().getAutoStart());
        }

        // 创建 Runner
        runner = RunnerFactory.create(envManager, spiderPath);
        if (runner == null) {
            throw new Exception("无法创建 Runner: " + spiderPath);
        }
        runner.setSiteKey(siteKey);

        // 启动桥接服务（如果需要）
        BinBridge.getInstance().start();

        // 初始化爬虫脚本
        String initResult = runner.init(extend);
        BinLogger.d("爬虫初始化结果: " + initResult);

        initialized = true;
        BinLogger.i("BinRunner 初始化完成: " + spiderPath);
    }

    @Override
    public String homeContent(boolean filter) throws Exception {
        checkInitialized();
        BinLogger.d("调用 homeContent, filter=" + filter);
        return runner.homeContent(filter);
    }

    @Override
    public String homeVideoContent() throws Exception {
        checkInitialized();
        BinLogger.d("调用 homeVideoContent");
        return runner.homeVideoContent();
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) throws Exception {
        checkInitialized();
        BinLogger.d("调用 categoryContent, tid=" + tid + ", pg=" + pg);
        return runner.categoryContent(tid, pg, filter, extend);
    }

    @Override
    public String detailContent(List<String> ids) throws Exception {
        checkInitialized();
        BinLogger.d("调用 detailContent, ids=" + ids);
        return runner.detailContent(ids);
    }

    @Override
    public String searchContent(String key, boolean quick) throws Exception {
        checkInitialized();
        BinLogger.d("调用 searchContent, key=" + key + ", quick=" + quick);
        return runner.searchContent(key, quick, "1");
    }

    @Override
    public String searchContent(String key, boolean quick, String pg) throws Exception {
        checkInitialized();
        BinLogger.d("调用 searchContent, key=" + key + ", quick=" + quick + ", pg=" + pg);
        return runner.searchContent(key, quick, pg);
    }

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) throws Exception {
        checkInitialized();
        BinLogger.d("调用 playerContent, flag=" + flag + ", id=" + id);
        return runner.playerContent(flag, id, vipFlags);
    }

    @Override
    public String liveContent(String url) throws Exception {
        checkInitialized();
        BinLogger.d("调用 liveContent, url=" + url);
        return runner.liveContent(url);
    }

    @Override
    public boolean manualVideoCheck() throws Exception {
        return false;
    }

    @Override
    public boolean isVideoFormat(String url) throws Exception {
        return false;
    }

    @Override
    public Object[] proxy(Map<String, String> params) throws Exception {
        checkInitialized();
        BinLogger.d("调用 proxy, params=" + params);
        return runner.proxy(params);
    }

    @Override
    public String action(String action) throws Exception {
        checkInitialized();
        BinLogger.d("调用 action, action=" + action);
        return runner.action(action);
    }

    @Override
    public void destroy() {
        BinLogger.i("BinRunner 销毁");
        
        if (runner != null) {
            try {
                runner.destroy();
            } catch (Exception e) {
                BinLogger.e("销毁 Runner 失败", e);
            }
        }

        // 注意：不要在这里停止桥接服务和进程管理器
        // 因为可能有其他 BinRunner 实例在使用
        
        initialized = false;
    }

    /**
     * 检查是否已初始化
     */
    private void checkInitialized() throws Exception {
        if (!initialized || runner == null) {
            throw new Exception("BinRunner 未初始化");
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String path) {
        if (path == null) return "";
        int lastDot = path.lastIndexOf('.');
        return lastDot > 0 ? path.substring(lastDot + 1) : "";
    }
}
