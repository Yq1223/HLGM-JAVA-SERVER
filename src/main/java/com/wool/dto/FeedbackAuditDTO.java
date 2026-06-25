package com.wool.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class FeedbackAuditDTO {

    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "无效的状态")
    @Max(value = 2, message = "无效的状态")
    private Integer status;

    private String reply;

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }
}
