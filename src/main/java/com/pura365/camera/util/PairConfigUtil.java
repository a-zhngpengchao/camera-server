package com.pura365.camera.util;

import com.pura365.camera.model.PairConfig;

public class PairConfigUtil {

    // 从字段生成协议字符串：PAIR\nSSID\nPW\nTZ\nRegion\n
    public static String buildPairString(PairConfig config) {
        StringBuilder sb = new StringBuilder();
        sb.append("PAIR").append("\n");
        sb.append(nullToEmpty(config.getSsid())).append("\n");
        sb.append(nullToEmpty(config.getPassword())).append("\n");
        sb.append(nullToEmpty(config.getTimezone())).append("\n");
        sb.append(nullToEmpty(config.getRegion())).append("\n");
        return sb.toString();
    }

    // 从协议字符串解析出字段（扫码结果 / BLE 数据都走这一个）
    public static PairConfig parsePairString(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("配网内容为空");
        }

        // 兼容 \r\n 和 \n，保留空行（密码可能为空）
        String normalized = raw.replace("\r\n", "\n");
        // 去掉末尾多余的 \n
        while (normalized.endsWith("\n")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        String[] lines = normalized.split("\n", -1); // -1 保留尾部空串
        if (lines.length < 5) {
            throw new IllegalArgumentException("配网内容格式不正确，行数不足5行");
        }

        if (!"PAIR".equalsIgnoreCase(lines[0].trim())) {
            throw new IllegalArgumentException("配网内容首行不是 PAIR");
        }

        PairConfig cfg = new PairConfig();
        cfg.setSsid(lines[1].trim());
        cfg.setPassword(lines[2]); // 密码可以为空，不强制 trim
        cfg.setTimezone(lines[3].trim());
        cfg.setRegion(lines[4].trim());
        return cfg;
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
