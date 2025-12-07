package com.pura365.camera.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 时间戳验证工具类
 * 用于验证请求时间戳是否在有效期内
 */
public class TimeValidator {
    
    private static final Logger log = LoggerFactory.getLogger(TimeValidator.class);
    
    /**
     * 默认有效期：5分钟（300秒）
     */
    private static final long DEFAULT_VALID_DURATION_SECONDS = 300;
    
    /**
     * 验证时间戳是否在有效期内（默认5分钟）
     * 
     * @param exp 要验证的时间戳（秒）
     * @return true: 有效, false: 无效
     */
    public static boolean isValid(Long exp) {
        return isValid(exp, DEFAULT_VALID_DURATION_SECONDS);
    }
    
    /**
     * 验证时间戳是否在有效期内
     * 
     * @param exp 要验证的时间戳（秒）
     * @param validDurationSeconds 有效期（秒）
     * @return true: 有效, false: 无效
     */
    public static boolean isValid(Long exp, long validDurationSeconds) {
        if (exp == null) {
            log.warn("时间戳为空，验证失败");
            return false;
        }
        
        long currentTime = System.currentTimeMillis() / 1000;
        long diff = Math.abs(currentTime - exp);
        
        boolean valid = diff <= validDurationSeconds;
        
        if (!valid) {
            log.warn("时间戳验证失败 - 当前时间: {}, 请求时间: {}, 差值: {}秒, 允许范围: {}秒", 
                    currentTime, exp, diff, validDurationSeconds);
        }
        
        return valid;
    }
    
    /**
     * 获取当前UTC时间戳（秒）
     * 
     * @return 当前时间戳
     */
    public static long getCurrentTimestamp() {
        return System.currentTimeMillis() / 1000;
    }
}
