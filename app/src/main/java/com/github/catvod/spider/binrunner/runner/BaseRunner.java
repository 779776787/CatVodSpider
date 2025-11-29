package com.github.catvod.spider.binrunner.runner;

import com.github.catvod.spider.binrunner.core.BinExecutor;
import com.github.catvod.spider.binrunner.core.EnvManager;
import com.github.catvod.spider.binrunner.process.ProcessResult;
import com.github.catvod.spider.binrunner.util.BinLogger;
import com.github.catvod.spider.binrunner.util.EnvBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Runner 基类
 * 所有脚本执行器的抽象基类
 */
public abstract class BaseRunner {

    protected static final Gson gson = new Gson();
    
    protected final EnvManager envManager;
    protected final BinExecutor executor;
    protected final String spiderPath;
    protected final String ext;
    
    protected String siteKey;
    protected Map<String, String> extraEnv;

    /**
     * 创建 Runner
     * @param envManager 环境管理器
     * @param spiderPath 爬虫脚本路径
     */
    public BaseRunner(EnvManager envManager, String spiderPath) {
        this.envManager = envManager;
        this.executor = new BinExecutor(envManager);
        this.spiderPath = spiderPath;
        this.ext = getFileExtension(spiderPath);
        this.extraEnv = new HashMap<>();
    }

    /**
     * 获取解释器名称
     */
    protected abstract String getInterpreterName();

    /**
     * 获取 Runner 脚本名称
     */
    protected abstract String getRunnerScriptName();

    /**
     * 配置环境变量
     */
    protected abstract void configureEnvironment(EnvBuilder envBuilder);

    /**
     * 设置站点 Key
     */
    public void setSiteKey(String siteKey) {
        this.siteKey = siteKey;
    }

    /**
     * 添加额外环境变量
     */
    public void addEnv(String key, String value) {
        extraEnv.put(key, value);
    }

    /**
     * 调用爬虫方法
     * @param method 方法名
     * @param args 参数
     * @return JSON 结果
     */
    public String call(String method, Object... args) {
        try {
            // 构建命令参数
            String[] cmdArgs = buildArgs(method, args);
            
            // 构建环境变量
            EnvBuilder envBuilder = new EnvBuilder();
            configureEnvironment(envBuilder);
            envBuilder.merge(extraEnv);

            // 获取 Runner 脚本路径
            File runnerScript = new File(envManager.getRunnerDir(), getRunnerScriptName());
            if (!runnerScript.exists()) {
                BinLogger.e("Runner 脚本不存在: " + runnerScript.getAbsolutePath());
                return errorResult("Runner 脚本不存在: " + getRunnerScriptName());
            }

            // 执行脚本
            String[] fullArgs = new String[cmdArgs.length + 2];
            fullArgs[0] = runnerScript.getAbsolutePath();
            fullArgs[1] = spiderPath;
            System.arraycopy(cmdArgs, 0, fullArgs, 2, cmdArgs.length);

            ProcessResult result = executor.executeScript(getInterpreterName(), runnerScript.getAbsolutePath(), 
                    buildScriptArgs(spiderPath, method, args), envBuilder.getEnv());

            if (result.isSuccess()) {
                return parseOutput(result.getStdout());
            } else {
                BinLogger.e("执行失败: " + result.getStderr());
                return errorResult(result.getStderr());
            }

        } catch (Exception e) {
            BinLogger.e("调用方法失败: " + method, e);
            return errorResult(e.getMessage());
        }
    }

    /**
     * 构建脚本参数
     */
    protected String[] buildScriptArgs(String spiderPath, String method, Object... args) {
        String[] result = new String[args.length + 2];
        result[0] = spiderPath;
        result[1] = method;
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) {
                result[i + 2] = "";
            } else if (args[i] instanceof String) {
                result[i + 2] = (String) args[i];
            } else {
                result[i + 2] = gson.toJson(args[i]);
            }
        }
        return result;
    }

    /**
     * 构建参数数组
     */
    protected String[] buildArgs(String method, Object... args) {
        String[] result = new String[args.length + 1];
        result[0] = method;
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) {
                result[i + 1] = "";
            } else if (args[i] instanceof String) {
                result[i + 1] = (String) args[i];
            } else if (args[i] instanceof Boolean) {
                result[i + 1] = args[i].toString();
            } else if (args[i] instanceof Map || args[i] instanceof List) {
                result[i + 1] = gson.toJson(args[i]);
            } else {
                result[i + 1] = args[i].toString();
            }
        }
        return result;
    }

    /**
     * 解析输出，提取 JSON 结果
     */
    protected String parseOutput(String output) {
        if (output == null || output.isEmpty()) {
            return "";
        }

        // 尝试找到 JSON 开始位置
        int jsonStart = -1;
        for (int i = 0; i < output.length(); i++) {
            char c = output.charAt(i);
            if (c == '{' || c == '[') {
                jsonStart = i;
                break;
            }
        }

        if (jsonStart >= 0) {
            String jsonPart = output.substring(jsonStart);
            // 验证是否为有效 JSON
            try {
                JsonElement element = JsonParser.parseString(jsonPart);
                if (element != null) {
                    return jsonPart;
                }
            } catch (Exception e) {
                // 不是有效 JSON，尝试找到 JSON 结束位置
                int braceCount = 0;
                int bracketCount = 0;
                int jsonEnd = jsonPart.length();
                
                for (int i = 0; i < jsonPart.length(); i++) {
                    char c = jsonPart.charAt(i);
                    if (c == '{') braceCount++;
                    else if (c == '}') braceCount--;
                    else if (c == '[') bracketCount++;
                    else if (c == ']') bracketCount--;
                    
                    if (braceCount == 0 && bracketCount == 0 && i > 0) {
                        jsonEnd = i + 1;
                        break;
                    }
                }
                
                try {
                    String extracted = jsonPart.substring(0, jsonEnd);
                    JsonParser.parseString(extracted);
                    return extracted;
                } catch (Exception ignored) {
                }
            }
        }

        // 如果没有找到 JSON，返回原始输出
        return output;
    }

    /**
     * 生成错误结果
     */
    protected String errorResult(String message) {
        return "{\"error\":\"" + escapeJson(message) + "\"}";
    }

    /**
     * 转义 JSON 字符串
     */
    protected String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    /**
     * 获取文件扩展名
     */
    protected String getFileExtension(String path) {
        int lastDot = path.lastIndexOf('.');
        if (lastDot > 0) {
            return path.substring(lastDot + 1).toLowerCase();
        }
        return "";
    }

    /**
     * 初始化
     */
    public String init(String extend) {
        return call("init", extend);
    }

    /**
     * 首页内容
     */
    public String homeContent(boolean filter) {
        return call("homeContent", filter);
    }

    /**
     * 首页视频内容
     */
    public String homeVideoContent() {
        return call("homeVideoContent");
    }

    /**
     * 分类内容
     */
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) {
        return call("categoryContent", tid, pg, filter, extend);
    }

    /**
     * 详情内容
     */
    public String detailContent(List<String> ids) {
        return call("detailContent", ids);
    }

    /**
     * 搜索内容
     */
    public String searchContent(String key, boolean quick, String pg) {
        return call("searchContent", key, quick, pg);
    }

    /**
     * 播放内容
     */
    public String playerContent(String flag, String id, List<String> vipFlags) {
        return call("playerContent", flag, id, vipFlags);
    }

    /**
     * 直播内容
     */
    public String liveContent(String url) {
        return call("liveContent", url);
    }

    /**
     * 代理请求
     */
    public Object[] proxy(Map<String, String> params) {
        String result = call("proxy", params);
        // 解析代理结果
        try {
            // 返回格式：[code, contentType, data]
            return new Object[]{200, "text/plain", result};
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 动作
     */
    public String action(String action) {
        return call("action", action);
    }

    /**
     * 销毁
     */
    public void destroy() {
        call("destroy");
    }
}
