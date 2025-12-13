package com.pura365.camera.model.share;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 扫码绑定设备请求
 */
@Data
public class ShareBindRequest {

    /**
     * 分享码
     * 可以是纯分享码，也可以是带前缀的格式：PURA365_SHARE:XXXXXXXX
     */
    @JsonProperty("share_code")
    private String shareCode;
}
