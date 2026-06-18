package com.wool.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wool.common.BizException;
import com.wool.common.PointsChangeType;
import com.wool.entity.PointsLog;
import com.wool.entity.User;
import com.wool.mapper.PointsLogMapper;
import com.wool.mapper.UserMapper;
import com.wool.service.PointsService;
import com.wool.vo.PointsLogVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
public class PointsServiceImpl implements PointsService {

    private static final Logger log = LoggerFactory.getLogger(PointsServiceImpl.class);

    private final UserMapper userMapper;
    private final PointsLogMapper pointsLogMapper;

    public PointsServiceImpl(UserMapper userMapper, PointsLogMapper pointsLogMapper) {
        this.userMapper = userMapper;
        this.pointsLogMapper = pointsLogMapper;
    }

    @Override
    @Transactional
    public void addPoints(Long userId, int points, int changeType, String remark, Long bizId) {
        if (points <= 0) {
            throw new BizException("积分增加数必须大于0");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }

        int before = user.getPoints();
        int after = before + points;

        user.setPoints(after);
        int rows = userMapper.updateById(user);
        if (rows == 0) {
            throw new BizException("积分更新失败，请重试");
        }

        PointsLog logEntity = new PointsLog();
        logEntity.setUserId(userId);
        logEntity.setChangeType(changeType);
        logEntity.setChangeValue(points);
        logEntity.setBeforePoints(before);
        logEntity.setAfterPoints(after);
        logEntity.setRemark(remark);
        logEntity.setBizId(bizId);
        pointsLogMapper.insert(logEntity);

        log.info("用户[{}]积分增加: {} → {} (+{})，原因: {}", userId, before, after, points, remark);
    }

    @Override
    @Transactional
    public void deductPoints(Long userId, int points, int changeType, String remark, Long bizId) {
        if (points <= 0) {
            throw new BizException("积分扣减数必须大于0");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }

        if (user.getPoints() < points) {
            throw new BizException("积分不足，当前积分: " + user.getPoints());
        }

        int before = user.getPoints();
        int after = before - points;

        user.setPoints(after);
        int rows = userMapper.updateById(user);
        if (rows == 0) {
            throw new BizException("积分更新失败，请重试");
        }

        PointsLog logEntity = new PointsLog();
        logEntity.setUserId(userId);
        logEntity.setChangeType(changeType);
        logEntity.setChangeValue(-points);
        logEntity.setBeforePoints(before);
        logEntity.setAfterPoints(after);
        logEntity.setRemark(remark);
        logEntity.setBizId(bizId);
        pointsLogMapper.insert(logEntity);

        log.info("用户[{}]积分扣减: {} → {} (-{})，原因: {}", userId, before, after, points, remark);
    }

    @Override
    public Page<PointsLogVO> getPointsLog(Long userId, int pageNum, int pageSize) {
        Page<PointsLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PointsLog> wrapper = new LambdaQueryWrapper<PointsLog>()
                .eq(PointsLog::getUserId, userId)
                .orderByDesc(PointsLog::getCreatedAt);

        Page<PointsLog> result = pointsLogMapper.selectPage(page, wrapper);

        Page<PointsLogVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(log -> {
            PointsLogVO vo = new PointsLogVO();
            BeanUtils.copyProperties(log, vo);
            PointsChangeType type = PointsChangeType.values()[log.getChangeType() - 1];
            vo.setChangeTypeDesc(type.desc);
            return vo;
        }).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public Integer getPoints(Long userId) {
        User user = userMapper.selectById(userId);
        return user != null ? user.getPoints() : 0;
    }
}
