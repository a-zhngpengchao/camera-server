package com.pura365.camera.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * MQTT 加密工具类
 *
 * 加密规则：
 *  - 明文为 JSON:{"code": Int, "time": Int64}
 *  - 加密算法：AES-128-ECB / NoPadding（明文右侧用空格补齐到16字节整数倍）
 *  - AES 密钥：WiFi SSID 的 MD5 原始 16 字节（不是十六进制字符串），本例 SSID = "AOCCX"
 */
public class MqttEncryptUtil {

    private static final String SSID = "SGHome";

    /**
     * 使用 SSID 的 MD5 作为 AES-128-ECB 密钥，加密 {code,time} JSON，返回原始密文字节数组
     */
    public static byte[] encrypt(int code, long time) throws Exception {
        // 1. 组装 JSON
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("code", code);
        node.put("time", time);
        String json = mapper.writeValueAsString(node);
        System.out.println("要加密的 JSON 明文: " + json);
        // 2. 生成 AES 密钥：MD5(SSID) 的原始 16 字节
        byte[] keyBytes = md5(SSID);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

        // 3. AES-128-ECB/NoPadding 加密，明文手动用空格补齐到16字节整数倍
        byte[] plainPadded = padWithSpaces(json.getBytes(StandardCharsets.UTF_8));
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] encrypted = cipher.doFinal(plainPadded);

        // 4. 返回原始密文字节数组，由调用方直接作为 MQTT payload 发送
        return encrypted;
    }

    /**
     * 计算字符串的 MD5，返回原始 16 字节
     */
    private static byte[] md5(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        return md.digest(input.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 使用空格将明文字节补齐到16字节整数倍（用于 AES/ECB/NoPadding）
     */
    private static byte[] padWithSpaces(byte[] data) {
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
     * 将字节数组转为十六进制字符串（大写）
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    /**
     * 将十六进制字符串（可包含空格）转换为字节数组
     */
    public static byte[] hexToBytes(String hex) {
        hex = hex.replace(" ", "");
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
        }
        return data;
    }

    /**
     * 解密十六进制表示的密文，返回明文 JSON 字符串
     */
    public static String decryptHex(String hexCipher) throws Exception {
        byte[] cipherBytes = hexToBytes(hexCipher);
        byte[] keyBytes = md5(SSID);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        byte[] plainBytes = cipher.doFinal(cipherBytes);
        // 去掉右侧补齐的空格
        return new String(plainBytes, StandardCharsets.UTF_8).trim();
    }

    /**
     * 尝试多种 key / 算法组合解密一条十六进制密文，打印所有尝试结果
     */
    public static void bruteForceDecrypt(String hexCipher) {
        System.out.println("===== 开始尝试多种 key / 算法 组合解密 =====");
        try {
            byte[] cipherBytes = hexToBytes(hexCipher);

            byte[] md5raw = md5(SSID);                          // 16 字节原始 MD5
            String md5hex = bytesToHex(md5raw).toLowerCase();   // 32 字符十六进制
            byte[] md5hexBytes = md5hex.getBytes(StandardCharsets.UTF_8); // 长度 32

            byte[][] candidateKeys = new byte[][]{
                    md5raw,                                   // 方案A：原始16字节 -> AES-128（当前确认使用）
                    Arrays.copyOf(md5hexBytes, 16),           // 方案B：hex字符串前16字节 -> AES-128
                    md5hexBytes                               // 方案C：hex字符串全部32字节 -> AES-256
            };

            String[] algos = new String[]{
                    "AES/ECB/PKCS5Padding",
                    "AES/ECB/NoPadding"                      // 当前确认使用
            };

            for (byte[] key : candidateKeys) {
                for (String algo : algos) {
                    try {
                        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
                        Cipher cipher = Cipher.getInstance(algo);
                        cipher.init(Cipher.DECRYPT_MODE, keySpec);
                        byte[] plain = cipher.doFinal(cipherBytes);
                        String s = new String(plain, StandardCharsets.UTF_8);
                        System.out.println("[成功] keyLen=" + key.length + ", algo=" + algo + " => " + s);
                    } catch (Exception e) {
                        System.out.println("[失败] keyLen=" + key.length + ", algo=" + algo + " => " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("bruteForceDecrypt 总体异常: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("===== 多种组合解密尝试结束 =====");
    }

    // 简单自测：生成一条要发送的 MQTT 密文负载并打印出来，同时解析一条对端报文
    public static void main(String[] args) throws Exception {
        // 这里模拟一条要发送的业务数据：code + time
        int code = 13;
        long time = System.currentTimeMillis() / 1000; // 当前时间戳（秒）
        System.out.println(time);
        byte[] cipherBytes = encrypt(code, time);

        // 明文部分（仅用于你自己查看，不发送）
        System.out.println("明文 JSON => {\"code\":" + code + ", \"time\":" + time + "}");
        System.out.println("密文字节长度 => " + cipherBytes.length);

        // 要发送的内容：密文字节的 16 进制表示（用于 MQTTX Hex 模式粘贴）
        StringBuilder hex = new StringBuilder();
        for (byte b : cipherBytes) {
            hex.append(String.format("%02X", b));
        }
        System.out.println("==================== 要发送的 MQTT 负载 ====================");
        System.out.println("(在 MQTTX 中选择 Hex/十六进制 模式，将下面这一行作为 Payload)");
        System.out.println(hex.toString());
        System.out.println("===========================================================");

        // ====== 下面是解析你贴出来的那条对端报文 ======
        String receivedHex = "c3dd 8a98 691b c47c 8649 384e ffd7 ca91 3e1f a009 2525 d007 dae4 e002 3f34 843c d6f0 3dc1 1dc9 fec6 21ea f1ec fb43 e09a 0ad0 bdbd af4c 7f61 17eb c669 c7aa d137 ec67 50a1 5d2a f74f e1c2 6608 8169 aaec 90c2 05e6 207d ee45 dcac 826e 7cb5 f5cb 61eb 69bc 1211 be44 8592 14ed 417c a871 d478 2292 a2c8 b572 ef9b 3ce4 17e3 438b f590 e1d8 796e c24f 6cd6 6f2f 4dde b8e1 520f 989c efa8 726e 2fd5 5315 9b63 b77a cc32 253a 9705 e44d 8147 5e19 bea1 b3d0 0ea1 2550 68ee 376f 7b34 00e2 1728 a58d 0e6d fe4a ac79 32ab eca2 b926 0c18 2077 7021 9ca2 edab ec45 9a3d 72c8 4b29 edbb d55a bfef f8d1 abb4 2f37 7fe3 43a9 001f";
        System.out.println("对端发来的十六进制密文 => " + receivedHex);
        try {
            String decryptedJson = decryptHex(receivedHex);
            System.out.println("对端报文解密后明文 JSON => " + decryptedJson);
        } catch (Exception e) {
            System.out.println("对端报文解密失败: " + e.getMessage());
        }

        // 使用多种 key / 算法组合再尝试一次，看看有没有正常的明文
        bruteForceDecrypt(receivedHex);
    }

}
