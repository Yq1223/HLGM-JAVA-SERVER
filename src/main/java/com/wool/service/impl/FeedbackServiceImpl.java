package com.wool.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wool.common.BizException;
import com.wool.common.Constants;
import com.wool.dto.FeedbackAuditDTO;
import com.wool.dto.FeedbackDTO;
import com.wool.entity.Feedback;
import com.wool.entity.User;
import com.wool.mapper.FeedbackMapper;
import com.wool.mapper.UserMapper;
import com.wool.service.FeedbackService;
import com.wool.vo.FeedbackVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.List;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    private static final Logger log = LoggerFactory.getLogger(FeedbackServiceImpl.class);

    private final FeedbackMapper feedbackMapper;
    private final UserMapper userMapper;

    public FeedbackServiceImpl(FeedbackMapper feedbackMapper, UserMapper userMapper) {
        this.feedbackMapper = feedbackMapper;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public Long submit(FeedbackDTO dto, Long userId) {
        Feedback feedback = new Feedback();
        feedback.setUserId(userId);
        feedback.setTitle(dto.getTitle().trim());
        feedback.setContent(dto.getContent().trim());
        feedback.setStatus(0); // 待处理
        feedbackMapper.insert(feedback);
        log.info("用户[{}]提交反馈[id={}]，标题: {}", userId, feedback.getId(), feedback.getTitle());
        return feedback.getId();
    }

    @Override
    public Page<FeedbackVO> myList(Long userId, int pageNum, int pageSize, Integer status) {
        Page<Feedback> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<Feedback>()
                .eq(Feedback::getUserId, userId)
                .eq(status != null, Feedback::getStatus, status)
                .orderByDesc(Feedback::getCreatedAt);

        Page<Feedback> result = feedbackMapper.selectPage(page, wrapper);
        return convertPage(result);
    }

    @Override
    public Page<FeedbackVO> adminList(int pageNum, int pageSize, Integer status) {
        Page<Feedback> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(Feedback::getStatus, status);
        }
        wrapper.orderByDesc(Feedback::getCreatedAt);

        Page<Feedback> result = feedbackMapper.selectPage(page, wrapper);
        return convertPage(result);
    }

    @Override
    public FeedbackVO getDetail(Long id, Long userId, Integer userRole) {
        Feedback feedback = feedbackMapper.selectById(id);
        if (feedback == null) {
            throw new BizException("反馈不存在");
        }

        boolean isOwner = feedback.getUserId().equals(userId);
        boolean isAdmin = userRole != null && userRole == Constants.ROLE_ADMIN;

        if (!isOwner && !isAdmin) {
            throw new BizException("无权查看该反馈");
        }

        FeedbackVO vo = toVO(feedback);
        User author = userMapper.selectById(feedback.getUserId());
        if (author != null) {
            vo.setUserName(author.getNickname());
        }
        return vo;
    }

    @Override
    @Transactional
    public void audit(Long id, FeedbackAuditDTO dto, Long adminId) {
        Feedback feedback = feedbackMapper.selectById(id);
        if (feedback == null) {
            throw new BizException("反馈不存在");
        }

        feedback.setStatus(dto.getStatus());
        if (dto.getReply() != null) {
            feedback.setReply(dto.getReply().trim());
        }
        feedbackMapper.updateById(feedback);
        log.info("管理员[{}]处理反馈[id={}]，状态更新为: {}", adminId, id, dto.getStatus());
    }

    // ---------- 辅助方法 ----------

    private Page<FeedbackVO> convertPage(Page<Feedback> page) {
        Map<Long, String> authorMap = page.getRecords().stream()
                .map(Feedback::getUserId)
                .distinct()
                .map(uid -> userMapper.selectById(uid))
                .filter(u -> u != null)
                .collect(Collectors.toMap(User::getId, User::getNickname));

        Page<FeedbackVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(feedback -> {
            FeedbackVO vo = toVO(feedback);
            vo.setUserName(authorMap.getOrDefault(feedback.getUserId(), "匿名"));
            return vo;
        }).collect(Collectors.toList()));
        return voPage;
    }

    private FeedbackVO toVO(Feedback feedback) {
        FeedbackVO vo = new FeedbackVO();
        BeanUtils.copyProperties(feedback, vo);
        vo.setStatusDesc(getStatusDesc(feedback.getStatus()));
        return vo;
    }

    private String getStatusDesc(int status) {
        switch (status) {
            case 0: return "待处理";
            case 1: return "进行中";
            case 2: return "已完成";
            case 3: return "不处理";
            default: return "未知";
        }
    }
}
