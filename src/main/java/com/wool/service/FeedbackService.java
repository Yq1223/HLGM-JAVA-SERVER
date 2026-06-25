package com.wool.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wool.dto.FeedbackAuditDTO;
import com.wool.dto.FeedbackDTO;
import com.wool.vo.FeedbackVO;

public interface FeedbackService {

    /**
     * 提交反馈
     */
    Long submit(FeedbackDTO dto, Long userId);

    /**
     * 查询我的反馈列表
     */
    Page<FeedbackVO> myList(Long userId, int pageNum, int pageSize, Integer status);

    /**
     * 管理员查询所有反馈列表
     */
    Page<FeedbackVO> adminList(int pageNum, int pageSize, Integer status);

    /**
     * 获取反馈详情
     */
    FeedbackVO getDetail(Long id, Long userId, Integer userRole);

    /**
     * 管理员处理反馈
     */
    void audit(Long id, FeedbackAuditDTO dto, Long adminId);
}
