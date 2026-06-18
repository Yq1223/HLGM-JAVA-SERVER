package com.wool.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("t_points_log")
public class PointsLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer changeType;
    private Integer changeValue;
    private Integer beforePoints;
    private Integer afterPoints;
    private String remark;
    private Long bizId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Integer getChangeType() { return changeType; }
    public void setChangeType(Integer changeType) { this.changeType = changeType; }

    public Integer getChangeValue() { return changeValue; }
    public void setChangeValue(Integer changeValue) { this.changeValue = changeValue; }

    public Integer getBeforePoints() { return beforePoints; }
    public void setBeforePoints(Integer beforePoints) { this.beforePoints = beforePoints; }

    public Integer getAfterPoints() { return afterPoints; }
    public void setAfterPoints(Integer afterPoints) { this.afterPoints = afterPoints; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public Long getBizId() { return bizId; }
    public void setBizId(Long bizId) { this.bizId = bizId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
