package com.github.catvod.spider.binrunner.util;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件操作工具类
 * 提供文件复制、读写、权限设置等功能
 */
public class FileHelper {

    private static final int BUFFER_SIZE = 16384;

    /**
     * 从 assets 复制文件到目标路径
     * @param context 上下文
     * @param assetPath assets 中的路径
     * @param destPath 目标路径
     * @return 是否成功
     */
    public static boolean copyFromAssets(Context context, String assetPath, String destPath) {
        try {
            File destFile = new File(destPath);
            ensureParentDir(destFile);
            
            try (InputStream in = context.getAssets().open(assetPath);
                 OutputStream out = new FileOutputStream(destFile)) {
                copyStream(in, out);
            }
            return true;
        } catch (IOException e) {
            BinLogger.e("从 assets 复制文件失败: " + assetPath, e);
            return false;
        }
    }

    /**
     * 递归复制 assets 目录
     * @param context 上下文
     * @param assetDir assets 中的目录
     * @param destDir 目标目录
     * @return 是否成功
     */
    public static boolean copyAssetDir(Context context, String assetDir, String destDir) {
        try {
            String[] files = context.getAssets().list(assetDir);
            if (files == null || files.length == 0) {
                return copyFromAssets(context, assetDir, destDir);
            }

            File destFile = new File(destDir);
            if (!destFile.exists()) {
                destFile.mkdirs();
            }

            for (String file : files) {
                String srcPath = assetDir.isEmpty() ? file : assetDir + "/" + file;
                String dstPath = destDir + "/" + file;
                copyAssetDir(context, srcPath, dstPath);
            }
            return true;
        } catch (IOException e) {
            BinLogger.e("复制 assets 目录失败: " + assetDir, e);
            return false;
        }
    }

    /**
     * 复制文件
     */
    public static boolean copyFile(File src, File dest) {
        try {
            ensureParentDir(dest);
            try (InputStream in = new FileInputStream(src);
                 OutputStream out = new FileOutputStream(dest)) {
                copyStream(in, out);
            }
            return true;
        } catch (IOException e) {
            BinLogger.e("复制文件失败: " + src.getAbsolutePath(), e);
            return false;
        }
    }

    /**
     * 复制流
     */
    public static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        out.flush();
    }

    /**
     * 设置文件可执行权限
     */
    public static boolean setExecutable(File file) {
        if (file.exists()) {
            boolean result = file.setExecutable(true, false);
            result &= file.setReadable(true, false);
            // 尝试使用 chmod 命令
            try {
                Runtime.getRuntime().exec("chmod 755 " + file.getAbsolutePath()).waitFor();
            } catch (Exception ignored) {
            }
            return result;
        }
        return false;
    }

    /**
     * 递归设置目录下所有二进制文件的可执行权限
     */
    public static void setExecutableRecursive(File dir) {
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                setExecutableRecursive(file);
            } else {
                setExecutable(file);
            }
        }
    }

    /**
     * 读取文件内容
     */
    public static String readFile(File file) {
        try {
            byte[] data = new byte[(int) file.length()];
            try (FileInputStream fis = new FileInputStream(file)) {
                int offset = 0;
                int remaining = data.length;
                while (remaining > 0) {
                    int bytesRead = fis.read(data, offset, remaining);
                    if (bytesRead == -1) {
                        break;
                    }
                    offset += bytesRead;
                    remaining -= bytesRead;
                }
            }
            return new String(data, StandardCharsets.UTF_8);
        } catch (IOException e) {
            BinLogger.e("读取文件失败: " + file.getAbsolutePath(), e);
            return "";
        }
    }

    /**
     * 写入文件内容
     */
    public static boolean writeFile(File file, String content) {
        try {
            ensureParentDir(file);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(content.getBytes(StandardCharsets.UTF_8));
                fos.flush();
            }
            return true;
        } catch (IOException e) {
            BinLogger.e("写入文件失败: " + file.getAbsolutePath(), e);
            return false;
        }
    }

    /**
     * 确保父目录存在
     */
    public static void ensureParentDir(File file) {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
    }

    /**
     * 确保目录存在
     */
    public static void ensureDir(File dir) {
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * 删除文件或目录
     */
    public static boolean delete(File file) {
        if (!file.exists()) {
            return true;
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    delete(f);
                }
            }
        }
        return file.delete();
    }

    /**
     * 列出目录下的所有文件
     */
    public static List<File> listFiles(File dir, String extension) {
        List<File> result = new ArrayList<>();
        if (!dir.exists() || !dir.isDirectory()) {
            return result;
        }
        File[] files = dir.listFiles();
        if (files == null) {
            return result;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                result.addAll(listFiles(file, extension));
            } else if (extension == null || file.getName().endsWith(extension)) {
                result.add(file);
            }
        }
        return result;
    }

    /**
     * 查找可执行文件
     * @param dir 搜索目录
     * @param name 文件名
     * @return 找到的文件，未找到返回 null
     */
    public static File findExecutable(File dir, String name) {
        if (!dir.exists() || !dir.isDirectory()) {
            return null;
        }
        
        // 先检查 bin 目录
        File binDir = new File(dir, "bin");
        if (binDir.exists()) {
            File exe = new File(binDir, name);
            if (exe.exists() && exe.canExecute()) {
                return exe;
            }
        }
        
        // 直接在目录下查找
        File exe = new File(dir, name);
        if (exe.exists() && exe.canExecute()) {
            return exe;
        }
        
        // 递归查找
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    File found = findExecutable(file, name);
                    if (found != null) {
                        return found;
                    }
                }
            }
        }
        
        return null;
    }

    /**
     * 获取文件扩展名
     */
    public static String getExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            return fileName.substring(lastDot + 1).toLowerCase();
        }
        return "";
    }

    /**
     * 检查文件是否存在且可读
     */
    public static boolean isReadable(File file) {
        return file.exists() && file.isFile() && file.canRead();
    }

    /**
     * 检查目录是否存在且可写
     */
    public static boolean isWritable(File dir) {
        return dir.exists() && dir.isDirectory() && dir.canWrite();
    }
}
