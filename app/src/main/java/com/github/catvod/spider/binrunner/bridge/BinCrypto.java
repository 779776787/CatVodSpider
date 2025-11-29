package com.github.catvod.spider.binrunner.bridge;

import android.util.Base64;

import com.github.catvod.spider.binrunner.util.BinLogger;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 加解密桥接类
 * 提供 MD5、AES、RSA 等加解密功能
 */
public class BinCrypto {

    /**
     * MD5 加密
     * @param input 输入字符串
     * @return MD5 哈希值（32位小写）
     */
    public static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            BigInteger no = new BigInteger(1, messageDigest);
            StringBuilder sb = new StringBuilder(no.toString(16));
            while (sb.length() < 32) {
                sb.insert(0, "0");
            }
            return sb.toString().toLowerCase();
        } catch (Exception e) {
            BinLogger.e("MD5 加密失败", e);
            return "";
        }
    }

    /**
     * MD5 加密（指定编码）
     * @param input 输入字符串
     * @param charset 字符编码
     * @return MD5 哈希值
     */
    public static String md5(String input, String charset) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes(charset));
            BigInteger no = new BigInteger(1, messageDigest);
            StringBuilder sb = new StringBuilder(no.toString(16));
            while (sb.length() < 32) {
                sb.insert(0, "0");
            }
            return sb.toString().toLowerCase();
        } catch (Exception e) {
            BinLogger.e("MD5 加密失败", e);
            return "";
        }
    }

    /**
     * AES CBC 加密
     * @param data 待加密数据
     * @param key 密钥
     * @param iv 初始向量
     * @return Base64 编码的密文
     */
    public static String aesEncrypt(String data, String key, String iv) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
            byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(encrypted, Base64.NO_WRAP);
        } catch (Exception e) {
            BinLogger.e("AES 加密失败", e);
            return "";
        }
    }

    /**
     * AES CBC 解密
     * @param data Base64 编码的密文
     * @param key 密钥
     * @param iv 初始向量
     * @return 解密后的明文
     */
    public static String aesDecrypt(String data, String key, String iv) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
            byte[] decrypted = cipher.doFinal(Base64.decode(data, Base64.DEFAULT));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            BinLogger.e("AES 解密失败", e);
            return "";
        }
    }

    /**
     * AES ECB 加密
     * @param data 待加密数据
     * @param key 密钥
     * @return Base64 编码的密文
     */
    public static String aesEcbEncrypt(String data, String key) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(encrypted, Base64.NO_WRAP);
        } catch (Exception e) {
            BinLogger.e("AES ECB 加密失败", e);
            return "";
        }
    }

    /**
     * AES ECB 解密
     * @param data Base64 编码的密文
     * @param key 密钥
     * @return 解密后的明文
     */
    public static String aesEcbDecrypt(String data, String key) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decrypted = cipher.doFinal(Base64.decode(data, Base64.DEFAULT));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            BinLogger.e("AES ECB 解密失败", e);
            return "";
        }
    }

    /**
     * RSA 公钥加密
     * @param data 待加密数据
     * @param publicKeyPem PEM 格式公钥
     * @return Base64 编码的密文
     */
    public static String rsaEncrypt(String data, String publicKeyPem) {
        try {
            String publicKeyPEM = publicKeyPem
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] decoded = Base64.decode(publicKeyPEM, Base64.DEFAULT);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(spec);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(encrypted, Base64.NO_WRAP);
        } catch (Exception e) {
            BinLogger.e("RSA 加密失败", e);
            return "";
        }
    }

    /**
     * RSA 私钥解密
     * @param data Base64 编码的密文
     * @param privateKeyPem PEM 格式私钥
     * @return 解密后的明文
     */
    public static String rsaDecrypt(String data, String privateKeyPem) {
        try {
            String privateKeyPEM = privateKeyPem
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] privateKeyBytes = Base64.decode(privateKeyPEM, Base64.DEFAULT);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decrypted = cipher.doFinal(Base64.decode(data, Base64.DEFAULT));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            BinLogger.e("RSA 解密失败", e);
            return "";
        }
    }

    /**
     * Base64 编码
     * @param data 待编码数据
     * @return Base64 字符串
     */
    public static String base64Encode(String data) {
        return Base64.encodeToString(data.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
    }

    /**
     * Base64 解码
     * @param data Base64 字符串
     * @return 解码后的数据
     */
    public static String base64Decode(String data) {
        try {
            byte[] decoded = Base64.decode(data, Base64.DEFAULT);
            return new String(decoded, StandardCharsets.UTF_8);
        } catch (Exception e) {
            BinLogger.e("Base64 解码失败", e);
            return "";
        }
    }

    /**
     * 生成随机密钥
     * @param size 密钥长度
     * @return 随机密钥
     */
    public static String randomKey(int size) {
        StringBuilder key = new StringBuilder();
        String keys = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for (int i = 0; i < size; i++) {
            key.append(keys.charAt((int) Math.floor(Math.random() * keys.length())));
        }
        return key.toString();
    }
}
