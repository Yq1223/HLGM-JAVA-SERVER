package com.wool.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wool.vo.PointsLogVO;

public interface PointsService {

    /**
     * 增加积分(发布奖励)
     */
    void addPoints(Long userId, int points, int changeType, String remark, Long bizId);

    /**
     * 扣减积分(兑换)
     */
    void deductPoints(Long userId, int points, int changeType, String remark, Long bizId);

    /**
     * 查询积分变动记录
     */
    Page<PointsLogVO> getPointsLog(Long userId, int pageNum, int pageSize);

    /**
     * 获取用户当前积分
     */
    Integer getPoints(Long userId);
}
