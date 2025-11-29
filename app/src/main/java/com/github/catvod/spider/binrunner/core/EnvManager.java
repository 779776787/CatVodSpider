package com.github.catvod.spider.binrunner.core;

import android.content.Context;

import com.github.catvod.spider.binrunner.config.EnvsConfig;
import com.github.catvod.spider.binrunner.util.BinLogger;
import com.github.catvod.spider.binrunner.util.FileHelper;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 环境管理器
 * 负责管理二进制运行环境，包括：
 * 1. 从用户目录复制二进制文件到私有目录
 * 2. 设置可执行权限
 * 3. 扫描可用的解释器
 */
public class EnvManager {

    // 默认用户目录路径（可通过环境变量 BINRUNNER_ENVS_DIR 覆盖）
    private static final String DEFAULT_USER_ENVS_DIR = "/storage/emulated/0/TV/envs";
    
    // 用户目录路径（运行时确定）
    public static String USER_ENVS_DIR = getEnvsDirFromEnvironment();
    // 用户配置文件路径
    public static String USER_CONFIG_FILE = USER_ENVS_DIR + "/envs.json";
    
    /**
     * 从环境变量获取用户目录路径
     */
    private static String getEnvsDirFromEnvironment() {
        String envDir = System.getenv("BINRUNNER_ENVS_DIR");
        if (envDir != null && !envDir.isEmpty()) {
            return envDir;
        }
        return DEFAULT_USER_ENVS_DIR;
    }

    // 私有目录名称
    private static final String PRIVATE_DIR_NAME = "binrunner";
    // 二进制目录名称
    private static final String ENVS_DIR_NAME = "envs";
    // Runner 脚本目录名称
    private static final String RUNNER_DIR_NAME = "runner";
    // JS 库目录名称
    private static final String JS_LIB_DIR_NAME = "js/lib";
    // Python 基类目录名称
    private static final String PYTHON_BASE_DIR_NAME = "python/base";
    // PHP 基类目录名称
    private static final String PHP_BASE_DIR_NAME = "php/base";
    // Lua 基类目录名称
    private static final String LUA_BASE_DIR_NAME = "lua/base";

    private static EnvManager instance;

    private Context context;
    private EnvsConfig config;
    private File privateDir;
    private File envsDir;
    private File runnerDir;
    private File jsLibDir;
    private File pythonBaseDir;
    private File phpBaseDir;
    private File luaBaseDir;

    // 已发现的解释器缓存
    private final Map<String, File> interpreters = new HashMap<>();

    private EnvManager() {
    }

    /**
     * 获取单例实例
     */
    public static synchronized EnvManager getInstance() {
        if (instance == null) {
            instance = new EnvManager();
        }
        return instance;
    }

    /**
     * 初始化环境管理器
     * @param context 应用上下文
     * @return 是否初始化成功
     */
    public boolean init(Context context) {
        this.context = context.getApplicationContext();
        
        // 初始化私有目录
        privateDir = new File(context.getFilesDir(), PRIVATE_DIR_NAME);
        envsDir = new File(privateDir, ENVS_DIR_NAME);
        runnerDir = new File(privateDir, RUNNER_DIR_NAME);
        jsLibDir = new File(privateDir, JS_LIB_DIR_NAME);
        pythonBaseDir = new File(privateDir, PYTHON_BASE_DIR_NAME);
        phpBaseDir = new File(privateDir, PHP_BASE_DIR_NAME);
        luaBaseDir = new File(privateDir, LUA_BASE_DIR_NAME);

        // 创建目录
        createDirectories();

        // 加载配置
        loadConfig();

        // 从 assets 复制运行时文件
        copyRuntimeFiles();

        // 从用户目录复制二进制文件
        copyBinaries();

        // 扫描解释器
        scanInterpreters();

        BinLogger.i("环境管理器初始化完成");
        return true;
    }

    /**
     * 创建必要的目录
     */
    private void createDirectories() {
        FileHelper.ensureDir(privateDir);
        FileHelper.ensureDir(envsDir);
        FileHelper.ensureDir(runnerDir);
        FileHelper.ensureDir(jsLibDir);
        FileHelper.ensureDir(pythonBaseDir);
        FileHelper.ensureDir(phpBaseDir);
        FileHelper.ensureDir(luaBaseDir);
    }

    /**
     * 加载配置文件
     */
    private void loadConfig() {
        File configFile = new File(USER_CONFIG_FILE);
        if (configFile.exists()) {
            String json = FileHelper.readFile(configFile);
            config = EnvsConfig.fromJson(json);
            BinLogger.i("已加载用户配置: " + USER_CONFIG_FILE);
        } else {
            config = EnvsConfig.getDefault();
            BinLogger.i("使用默认配置");
        }
        
        // 初始化日志
        BinLogger.getInstance().init(config.getSettings().getLog());
    }

    /**
     * 从 assets 复制运行时文件
     */
    private void copyRuntimeFiles() {
        try {
            // 复制 runner 脚本
            FileHelper.copyAssetDir(context, "binrunner/runner", runnerDir.getAbsolutePath());
            BinLogger.d("已复制 runner 脚本");

            // 复制 JS 库
            FileHelper.copyAssetDir(context, "binrunner/js/lib", jsLibDir.getAbsolutePath());
            BinLogger.d("已复制 JS 库");

            // 复制 Python 基类
            FileHelper.copyAssetDir(context, "binrunner/python/base", pythonBaseDir.getAbsolutePath());
            BinLogger.d("已复制 Python 基类");

            // 复制 PHP 基类
            FileHelper.copyAssetDir(context, "binrunner/php/base", phpBaseDir.getAbsolutePath());
            BinLogger.d("已复制 PHP 基类");

            // 复制 Lua 基类
            FileHelper.copyAssetDir(context, "binrunner/lua/base", luaBaseDir.getAbsolutePath());
            BinLogger.d("已复制 Lua 基类");

        } catch (Exception e) {
            BinLogger.e("复制运行时文件失败", e);
        }
    }

    /**
     * 从用户目录复制二进制文件
     */
    private void copyBinaries() {
        File userEnvsDir = new File(USER_ENVS_DIR);
        if (!userEnvsDir.exists()) {
            BinLogger.w("用户环境目录不存在: " + USER_ENVS_DIR);
            return;
        }

        File[] envDirs = userEnvsDir.listFiles(File::isDirectory);
        if (envDirs == null) {
            return;
        }

        for (File envDir : envDirs) {
            String name = envDir.getName();
            if ("logs".equals(name)) {
                continue;
            }

            File destDir = new File(envsDir, name);
            FileHelper.ensureDir(destDir);

            // 复制 bin 目录
            File binDir = new File(envDir, "bin");
            if (binDir.exists()) {
                File destBinDir = new File(destDir, "bin");
                FileHelper.ensureDir(destBinDir);
                copyBinDir(binDir, destBinDir);
            }

            // 复制 lib 目录（如果存在）
            File libDir = new File(envDir, "lib");
            if (libDir.exists()) {
                File destLibDir = new File(destDir, "lib");
                copyDir(libDir, destLibDir);
            }
        }
    }

    /**
     * 复制 bin 目录并设置可执行权限
     */
    private void copyBinDir(File srcDir, File destDir) {
        File[] files = srcDir.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            File destFile = new File(destDir, file.getName());
            if (file.isDirectory()) {
                FileHelper.ensureDir(destFile);
                copyBinDir(file, destFile);
            } else {
                // 检查是否需要更新（目标不存在或源文件更新）
                if (!destFile.exists() || file.lastModified() > destFile.lastModified()) {
                    FileHelper.copyFile(file, destFile);
                    FileHelper.setExecutable(destFile);
                    BinLogger.d("已复制二进制: " + file.getName());
                }
            }
        }
    }

    /**
     * 复制目录
     */
    private void copyDir(File srcDir, File destDir) {
        FileHelper.ensureDir(destDir);
        File[] files = srcDir.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            File destFile = new File(destDir, file.getName());
            if (file.isDirectory()) {
                copyDir(file, destFile);
            } else {
                if (!destFile.exists() || file.lastModified() > destFile.lastModified()) {
                    FileHelper.copyFile(file, destFile);
                }
            }
        }
    }

    /**
     * 扫描可用的解释器
     */
    private void scanInterpreters() {
        interpreters.clear();

        File[] envDirs = envsDir.listFiles(File::isDirectory);
        if (envDirs == null) {
            return;
        }

        for (File envDir : envDirs) {
            File binDir = new File(envDir, "bin");
            if (!binDir.exists()) {
                continue;
            }

            File[] binFiles = binDir.listFiles(File::isFile);
            if (binFiles == null) {
                continue;
            }

            for (File binFile : binFiles) {
                if (binFile.canExecute()) {
                    interpreters.put(binFile.getName(), binFile);
                    BinLogger.d("发现解释器: " + binFile.getName() + " -> " + binFile.getAbsolutePath());
                }
            }
        }

        BinLogger.i("共发现 " + interpreters.size() + " 个解释器");
    }

    /**
     * 获取解释器路径
     * @param name 解释器名称或别名
     * @return 解释器文件，未找到返回 null
     */
    public File getInterpreter(String name) {
        // 首先解析别名
        String realName = config.resolveAlias(name);
        
        // 从缓存中查找
        File interpreter = interpreters.get(realName);
        if (interpreter != null && interpreter.exists()) {
            return interpreter;
        }

        // 在用户目录中查找
        File userInterpreter = FileHelper.findExecutable(new File(USER_ENVS_DIR), realName);
        if (userInterpreter != null) {
            return userInterpreter;
        }

        return null;
    }

    /**
     * 获取所有可用的解释器
     */
    public Map<String, File> getInterpreters() {
        return new HashMap<>(interpreters);
    }

    /**
     * 获取配置
     */
    public EnvsConfig getConfig() {
        return config;
    }

    /**
     * 重新加载配置
     */
    public void reloadConfig() {
        loadConfig();
        scanInterpreters();
    }

    /**
     * 获取私有目录
     */
    public File getPrivateDir() {
        return privateDir;
    }

    /**
     * 获取二进制目录
     */
    public File getEnvsDir() {
        return envsDir;
    }

    /**
     * 获取 Runner 脚本目录
     */
    public File getRunnerDir() {
        return runnerDir;
    }

    /**
     * 获取 JS 库目录
     */
    public File getJsLibDir() {
        return jsLibDir;
    }

    /**
     * 获取 Python 基类目录
     */
    public File getPythonBaseDir() {
        return pythonBaseDir;
    }

    /**
     * 获取 PHP 基类目录
     */
    public File getPhpBaseDir() {
        return phpBaseDir;
    }

    /**
     * 获取 Lua 基类目录
     */
    public File getLuaBaseDir() {
        return luaBaseDir;
    }

    /**
     * 获取特定解释器的 lib 目录
     */
    public File getLibDir(String interpreterName) {
        String realName = config.resolveAlias(interpreterName);
        File envDir = findEnvDirForInterpreter(realName);
        if (envDir != null) {
            File libDir = new File(envDir, "lib");
            if (libDir.exists()) {
                return libDir;
            }
        }
        return null;
    }

    /**
     * 查找解释器所在的环境目录
     */
    private File findEnvDirForInterpreter(String name) {
        File[] envDirs = envsDir.listFiles(File::isDirectory);
        if (envDirs == null) {
            return null;
        }

        for (File envDir : envDirs) {
            File binDir = new File(envDir, "bin");
            File binFile = new File(binDir, name);
            if (binFile.exists()) {
                return envDir;
            }
        }
        return null;
    }

    /**
     * 检查环境是否就绪
     */
    public boolean isReady() {
        return !interpreters.isEmpty();
    }
}
