package com.wool.dto;

import javax.validation.constraints.NotNull;

public class AuditDTO {

    @NotNull(message = "审核结果不能为空")
    private Integer action;

    private String rejectReason;

    public Integer getAction() { return action; }
    public void setAction(Integer action) { this.action = action; }

    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
}
