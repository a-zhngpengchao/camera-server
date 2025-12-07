package com.pura365.camera.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class SendMsgRequest {
    @NotBlank(message = "通知主题不能为空")
    private String topic;
    
    @NotBlank(message = "通知标题不能为空")
    private String title;
    
    @NotBlank(message = "通知内容不能为空")
    private String msg;
    
    @NotNull(message = "时间戳不能为空")
    private Long exp;
    
    // getters and setters
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }
    
    public Long getExp() { return exp; }
    public void setExp(Long exp) { this.exp = exp; }
    
    @Override
    public String toString() {
        return "SendMsgRequest{" +
                "topic='" + topic + '\'' +
                ", title='" + title + '\'' +
                ", msg='" + msg + '\'' +
                ", exp=" + exp +
                '}';
    }
}
