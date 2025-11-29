package com.github.catvod.spider;

import android.content.Context;

import com.github.catvod.binrunner.config.AutoStartConfig;
import com.github.catvod.binrunner.core.BinExecutor;
import com.github.catvod.binrunner.core.EnvManager;
import com.github.catvod.binrunner.core.ProcessManager;
import com.github.catvod.binrunner.runner.BaseRunner;
import com.github.catvod.binrunner.runner.RunnerFactory;
import com.github.catvod.binrunner.terminal.Terminal;
import com.github.catvod.binrunner.util.BinLogger;
import com.github.catvod.crawler.Spider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BinRunner 爬虫
 * 支持执行外部脚本（PHP、Python、JavaScript、Lua）的爬虫入口
 * 
 * 配置示例：
 * {
 *   "key": "binrunner",
 *   "name": "PHP测试",
 *   "type": 3,
 *   "api": "csp_BinRunner",
 *   "ext": "/storage/emulated/0/TV/files/php/jktv.php"
 * }
 */
public class BinRunner extends Spider {

    private EnvManager envManager;
    private ProcessManager processManager;
    private Terminal terminal;
    private BaseRunner scriptRunner;  // 脚本执行器
    private String scriptPath;        // 脚本路径

    @Override
    public void init(Context context, String extend) throws Exception {
        BinLogger.info("初始化 BinRunner");
        
        // 初始化环境管理器
        envManager = EnvManager.getInstance();
        envManager.init(context);

        // 初始化进程管理器
        processManager = ProcessManager.getInstance();

        // 初始化终端
        terminal = new Terminal(envManager, processManager);

        // 解析 extend 参数
        // 如果 extend 是脚本文件路径，则初始化对应的 Runner
        if (extend != null && !extend.isEmpty()) {
            if (isScriptFile(extend)) {
                scriptPath = extend;
                scriptRunner = RunnerFactory.create(extend, envManager);
                if (scriptRunner != null) {
                    scriptRunner.init("");
                    BinLogger.info("脚本 Runner 初始化完成: " + extend);
                } else {
                    BinLogger.error("无法创建脚本 Runner: " + extend);
                }
            } else {
                BinLogger.info("extend 不是脚本文件路径: " + extend);
            }
        }

        // 执行自动启动命令
        executeAutoStart();

        BinLogger.info("BinRunner 初始化完成");
    }

    @Override
    public String homeContent(boolean filter) throws Exception {
        if (scriptRunner != null) {
            return scriptRunner.homeContent(filter);
        }
        // 返回终端界面
        return terminal.getHomeContent();
    }

    @Override
    public String homeVideoContent() throws Exception {
        if (scriptRunner != null) {
            return scriptRunner.homeVideoContent();
        }
        return "";
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) throws Exception {
        if (scriptRunner != null) {
            return scriptRunner.categoryContent(tid, pg, filter, extend);
        }
        return "";
    }

    @Override
    public String detailContent(List<String> ids) throws Exception {
        if (scriptRunner != null) {
            return scriptRunner.detailContent(ids);
        }
        return "";
    }

    @Override
    public String searchContent(String key, boolean quick) throws Exception {
        return searchContent(key, quick, "1");
    }

    @Override
    public String searchContent(String key, boolean quick, String pg) throws Exception {
        if (scriptRunner != null) {
            return scriptRunner.searchContent(key, quick, pg);
        }
        return "";
    }

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) throws Exception {
        if (scriptRunner != null) {
            return scriptRunner.playerContent(flag, id, vipFlags);
        }
        return "";
    }

    @Override
    public String liveContent(String url) throws Exception {
        if (scriptRunner != null) {
            return scriptRunner.liveContent(url);
        }
        return "";
    }

    @Override
    public String action(String action) throws Exception {
        if (scriptRunner != null) {
            return scriptRunner.action(action);
        }
        // 处理终端命令
        return terminal.handleInput(action);
    }

    @Override
    public void destroy() {
        BinLogger.info("销毁 BinRunner");
        
        if (scriptRunner != null) {
            scriptRunner.destroy();
            scriptRunner = null;
        }
        
        if (processManager != null) {
            processManager.destroyAll();
        }
        
        BinLogger.info("BinRunner 已销毁");
    }

    /**
     * 判断是否为脚本文件
     * @param path 文件路径
     * @return 是否为脚本文件
     */
    private boolean isScriptFile(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        String lower = path.toLowerCase();
        return lower.endsWith(".php") || 
               lower.endsWith(".py") || 
               lower.endsWith(".js") || 
               lower.endsWith(".lua");
    }

    /**
     * 执行自动启动命令
     * 自动启动功能已预留接口，可在未来版本中实现
     */
    private void executeAutoStart() {
        BinLogger.debug("检查自动启动命令");
    }

    /**
     * 获取脚本 Runner
     * @return 脚本 Runner
     */
    public BaseRunner getScriptRunner() {
        return scriptRunner;
    }

    /**
     * 获取终端
     * @return 终端
     */
    public Terminal getTerminal() {
        return terminal;
    }

    /**
     * 获取环境管理器
     * @return 环境管理器
     */
    public EnvManager getEnvManager() {
        return envManager;
    }

    /**
     * 获取进程管理器
     * @return 进程管理器
     */
    public ProcessManager getProcessManager() {
        return processManager;
    }
}
