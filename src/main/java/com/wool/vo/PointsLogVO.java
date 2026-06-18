package com.wool.vo;

import java.time.LocalDateTime;

public class PointsLogVO {

    private Long id;
    private Integer changeType;
    private String changeTypeDesc;
    private Integer changeValue;
    private Integer beforePoints;
    private Integer afterPoints;
    private String remark;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getChangeType() { return changeType; }
    public void setChangeType(Integer changeType) { this.changeType = changeType; }

    public String getChangeTypeDesc() { return changeTypeDesc; }
    public void setChangeTypeDesc(String changeTypeDesc) { this.changeTypeDesc = changeTypeDesc; }

    public Integer getChangeValue() { return changeValue; }
    public void setChangeValue(Integer changeValue) { this.changeValue = changeValue; }

    public Integer getBeforePoints() { return beforePoints; }
    public void setBeforePoints(Integer beforePoints) { this.beforePoints = beforePoints; }

    public Integer getAfterPoints() { return afterPoints; }
    public void setAfterPoints(Integer afterPoints) { this.afterPoints = afterPoints; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
