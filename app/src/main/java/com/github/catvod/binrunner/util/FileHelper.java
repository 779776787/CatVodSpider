package com.github.catvod.binrunner.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * File operation utility for BinRunner.
 * Provides common file operations like copy, read, write, and permission setting.
 */
public class FileHelper {

    private static final int BUFFER_SIZE = 8192;

    /**
     * Read file content as string.
     *
     * @param file file to read
     * @return file content or empty string on error
     */
    public static String readString(File file) {
        if (file == null || !file.exists()) {
            return "";
        }
        
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = new byte[(int) file.length()];
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
            if (offset > 0) {
                return new String(data, 0, offset, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Write string content to file.
     *
     * @param file file to write
     * @param content content to write
     * @return true if successful
     */
    public static boolean writeString(File file, String content) {
        if (file == null || content == null) {
            return false;
        }
        
        try {
            ensureParentDir(file);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(content.getBytes(StandardCharsets.UTF_8));
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Copy file from source to destination.
     *
     * @param src source file
     * @param dst destination file
     * @return true if successful
     */
    public static boolean copyFile(File src, File dst) {
        if (src == null || !src.exists() || dst == null) {
            return false;
        }
        
        try {
            ensureParentDir(dst);
            try (InputStream is = new FileInputStream(src);
                 OutputStream os = new FileOutputStream(dst)) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Copy directory recursively.
     *
     * @param srcDir source directory
     * @param dstDir destination directory
     * @return true if successful
     */
    public static boolean copyDirectory(File srcDir, File dstDir) {
        if (srcDir == null || !srcDir.exists() || !srcDir.isDirectory()) {
            return false;
        }
        
        if (!dstDir.exists() && !dstDir.mkdirs()) {
            return false;
        }
        
        File[] files = srcDir.listFiles();
        if (files == null) {
            return true;
        }
        
        boolean success = true;
        for (File file : files) {
            File dstFile = new File(dstDir, file.getName());
            if (file.isDirectory()) {
                success &= copyDirectory(file, dstFile);
            } else {
                success &= copyFile(file, dstFile);
            }
        }
        return success;
    }

    /**
     * Set executable permission on file (755).
     *
     * @param file file to make executable
     * @return true if successful
     */
    public static boolean setExecutable(File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        
        try {
            // Set 755 permissions (rwxr-xr-x)
            // readable by all, writable by owner only, executable by all
            boolean result = file.setReadable(true, false);   // r for all
            result &= file.setWritable(true, true);           // w for owner only
            result &= file.setExecutable(true, false);        // x for all
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Set executable permission on all files in a directory.
     *
     * @param dir directory containing executables
     * @return true if all successful
     */
    public static boolean setExecutableDirectory(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return false;
        }
        
        File[] files = dir.listFiles();
        if (files == null) {
            return true;
        }
        
        boolean success = true;
        for (File file : files) {
            if (file.isFile()) {
                success &= setExecutable(file);
            }
        }
        return success;
    }

    /**
     * Delete file or directory recursively.
     *
     * @param file file or directory to delete
     * @return true if successful
     */
    public static boolean delete(File file) {
        if (file == null || !file.exists()) {
            return true;
        }
        
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    delete(child);
                }
            }
        }
        return file.delete();
    }

    /**
     * Ensure parent directory exists.
     *
     * @param file file whose parent should exist
     * @return true if parent exists or was created
     */
    public static boolean ensureParentDir(File file) {
        if (file == null) {
            return false;
        }
        File parent = file.getParentFile();
        if (parent == null) {
            return true;
        }
        return parent.exists() || parent.mkdirs();
    }

    /**
     * Check if file exists and is readable.
     *
     * @param file file to check
     * @return true if file exists and is readable
     */
    public static boolean isReadable(File file) {
        return file != null && file.exists() && file.canRead();
    }

    /**
     * Check if file is executable.
     *
     * @param file file to check
     * @return true if file is executable
     */
    public static boolean isExecutable(File file) {
        return file != null && file.exists() && file.canExecute();
    }

    /**
     * Get file extension.
     *
     * @param file file
     * @return extension without dot, or empty string
     */
    public static String getExtension(File file) {
        if (file == null) {
            return "";
        }
        return getExtension(file.getName());
    }

    /**
     * Get file extension from filename.
     *
     * @param filename filename
     * @return extension without dot, or empty string
     */
    public static String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        int lastDot = filename.lastIndexOf('.');
        return filename.substring(lastDot + 1).toLowerCase();
    }

    /**
     * Read input stream to string.
     * Useful for reading process output streams.
     *
     * @param stream input stream to read
     * @return string content
     */
    public static String readStream(InputStream stream) {
        if (stream == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append(line);
            }
        } catch (IOException e) {
            // Stream may be closed or interrupted
        }
        return sb.toString();
    }
}
