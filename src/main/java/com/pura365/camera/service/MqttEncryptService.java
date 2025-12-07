package com.pura365.camera.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * MQTT 消息加解密服务
 * 加密算法：AES-128-ECB / NoPadding
 * 密钥：WiFi SSID 的 MD5 值（原始 16 字节）
 */
@Service
public class MqttEncryptService {
    
    private static final Logger log = LoggerFactory.getLogger(MqttEncryptService.class);
    
    // TODO: 这个SSID需要从配置或数据库中获取，每个设备可能不同
    // 当前为了方便调试，先固定为 SGHome（注意大小写）
    private static final String DEFAULT_SSID = "SGHome";
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 加密消息
     * @param message 消息对象
     * @param ssid WiFi SSID（如果为null则使用默认值）
     * @return 加密后的字节数组
     */
    public byte[] encrypt(Object message, String ssid) throws Exception {
        // 1. 序列化为JSON
        String json = objectMapper.writeValueAsString(message);
        log.debug("加密前JSON: {}", json);
        
        // 2. 生成AES密钥：MD5(SSID)的原始16字节
        String actualSsid = (ssid != null && !ssid.isEmpty()) ? ssid : DEFAULT_SSID;
        byte[] keyBytes = md5(actualSsid);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        
        // 3. AES-128-ECB/NoPadding加密，明文手动用空格补齐到16字节整数倍
        byte[] plainPadded = padWithSpaces(json.getBytes(StandardCharsets.UTF_8));
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] encrypted = cipher.doFinal(plainPadded);
        
        log.debug("加密后字节长度: {}", encrypted.length);
        return encrypted;
    }
    
    /**
     * 解密消息
     * @param encryptedBytes 加密的字节数组
     * @param ssid WiFi SSID（如果为null则使用默认值）
     * @return 解密后的JSON字符串
     */
    public String decrypt(byte[] encryptedBytes, String ssid) throws Exception {
        // 1. 生成AES密钥
        String actualSsid = (ssid != null && !ssid.isEmpty()) ? ssid : DEFAULT_SSID;
        byte[] keyBytes = md5(actualSsid);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        
        // 2. AES-128-ECB/NoPadding解密
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        byte[] plainBytes = cipher.doFinal(encryptedBytes);
        
        // 3. 去掉右侧补齐的空格
        String json = new String(plainBytes, StandardCharsets.UTF_8).trim();
        log.debug("解密后JSON: {}", json);
        return json;
    }
    
    /**
     * 解密并反序列化为指定类型
     */
    public <T> T decryptAndParse(byte[] encryptedBytes, String ssid, Class<T> clazz) throws Exception {
        String json = decrypt(encryptedBytes, ssid);
        return objectMapper.readValue(json, clazz);
    }
    
    /**
     * 计算字符串的MD5，返回原始16字节
     */
    private byte[] md5(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        return md.digest(input.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * 使用空格将明文字节补齐到16字节整数倍（用于AES/ECB/NoPadding）
     */
    private byte[] padWithSpaces(byte[] data) {
        int blockSize = 16;
        int rem = data.length % blockSize;
        if (rem == 0) {
            return data;
        }
        int newLen = data.length + (blockSize - rem);
        byte[] out = Arrays.copyOf(data, newLen);
        Arrays.fill(out, data.length, newLen, (byte) ' ');
        return out;
    }
    
    /**
     * 将字节数组转为连续的十六进制（大写）字符串（原有调试方法）
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    /**
     * 将字节数组按 "f09a 3784 1927 ..." 这种格式输出：
     * - 每个字节用2位小写16进制表示
     * - 每2个字节一组，共4个16进制字符
     * - 组之间用空格分隔
     */
    public static String bytesToGroupedHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(bytes.length * 3);
        for (int i = 0; i < bytes.length; i++) {
            if (i > 0 && i % 2 == 0) {
                sb.append(' ');
            }
            sb.append(String.format("%02x", bytes[i]));
        }
        return sb.toString();
    }
}
