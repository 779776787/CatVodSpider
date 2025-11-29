package com.github.catvod.binrunner.util;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * 文件工具类
 * 提供文件操作相关的辅助方法
 */
public class FileHelper {

    /**
     * 检查文件是否存在
     * @param path 文件路径
     * @return 是否存在
     */
    public static boolean exists(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        return new File(path).exists();
    }

    /**
     * 检查文件是否可执行
     * @param path 文件路径
     * @return 是否可执行
     */
    public static boolean isExecutable(String path) {
        if (!exists(path)) {
            return false;
        }
        return new File(path).canExecute();
    }

    /**
     * 设置文件可执行权限
     * @param path 文件路径
     * @return 是否设置成功
     */
    public static boolean setExecutable(String path) {
        if (!exists(path)) {
            return false;
        }
        return new File(path).setExecutable(true, false);
    }

    /**
     * 读取文件内容
     * @param path 文件路径
     * @return 文件内容
     */
    public static String readFile(String path) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                return null;
            }
            byte[] data = new byte[(int) file.length()];
            try (FileInputStream fis = new FileInputStream(file)) {
                int bytesRead = fis.read(data);
                if (bytesRead > 0) {
                    return new String(data, 0, bytesRead, StandardCharsets.UTF_8);
                }
            }
        } catch (IOException e) {
            BinLogger.error("读取文件失败: " + path, e);
        }
        return null;
    }

    /**
     * 写入文件内容
     * @param path 文件路径
     * @param content 文件内容
     * @return 是否写入成功
     */
    public static boolean writeFile(String path, String content) {
        try {
            File file = new File(path);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                boolean created = parent.mkdirs();
                if (!created) {
                    BinLogger.error("创建目录失败: " + parent.getAbsolutePath());
                    return false;
                }
            }
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(content.getBytes(StandardCharsets.UTF_8));
            }
            return true;
        } catch (IOException e) {
            BinLogger.error("写入文件失败: " + path, e);
            return false;
        }
    }

    /**
     * 删除文件
     * @param path 文件路径
     * @return 是否删除成功
     */
    public static boolean delete(String path) {
        if (!exists(path)) {
            return true;
        }
        return new File(path).delete();
    }

    /**
     * 获取文件扩展名
     * @param path 文件路径
     * @return 扩展名（小写）
     */
    public static String getExtension(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        int lastDot = path.lastIndexOf('.');
        if (lastDot < 0 || lastDot == path.length() - 1) {
            return "";
        }
        return path.substring(lastDot + 1).toLowerCase();
    }

    /**
     * 获取文件名
     * @param path 文件路径
     * @return 文件名
     */
    public static String getFileName(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        int lastSep = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        if (lastSep < 0) {
            return path;
        }
        return path.substring(lastSep + 1);
    }

    /**
     * 获取目录路径
     * @param path 文件路径
     * @return 目录路径
     */
    public static String getDirectory(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        int lastSep = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        if (lastSep < 0) {
            return "";
        }
        return path.substring(0, lastSep);
    }

    /**
     * 从 assets 复制文件到目标目录
     * @param context 上下文
     * @param assetPath assets 中的路径
     * @param destPath 目标路径
     * @return 是否复制成功
     */
    public static boolean copyFromAssets(Context context, String assetPath, String destPath) {
        try {
            File destFile = new File(destPath);
            File parent = destFile.getParentFile();
            if (parent != null && !parent.exists()) {
                boolean created = parent.mkdirs();
                if (!created) {
                    return false;
                }
            }
            try (InputStream is = context.getAssets().open(assetPath);
                 OutputStream os = new FileOutputStream(destFile)) {
                byte[] buffer = new byte[4096];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
            }
            return true;
        } catch (IOException e) {
            BinLogger.error("从 assets 复制文件失败: " + assetPath, e);
            return false;
        }
    }

    /**
     * 确保目录存在
     * @param path 目录路径
     * @return 是否存在或创建成功
     */
    public static boolean ensureDirectory(String path) {
        File dir = new File(path);
        if (dir.exists()) {
            return dir.isDirectory();
        }
        return dir.mkdirs();
    }
}
