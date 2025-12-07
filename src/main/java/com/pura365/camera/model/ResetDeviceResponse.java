package com.pura365.camera.model;

public class ResetDeviceResponse {
    private Integer code;
    
    public ResetDeviceResponse(Integer code) {
        this.code = code;
    }
    
    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }
}