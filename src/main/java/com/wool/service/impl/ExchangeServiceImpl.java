package com.wool.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wool.common.BizException;
import com.wool.common.PointsChangeType;
import com.wool.dto.ExchangeDTO;
import com.wool.entity.ExchangeGoods;
import com.wool.entity.ExchangeRecord;
import com.wool.mapper.ExchangeGoodsMapper;
import com.wool.mapper.ExchangeRecordMapper;
import com.wool.service.ExchangeService;
import com.wool.service.PointsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExchangeServiceImpl implements ExchangeService {

    private static final Logger log = LoggerFactory.getLogger(ExchangeServiceImpl.class);

    private final ExchangeGoodsMapper goodsMapper;
    private final ExchangeRecordMapper recordMapper;
    private final PointsService pointsService;

    public ExchangeServiceImpl(ExchangeGoodsMapper goodsMapper,
                               ExchangeRecordMapper recordMapper,
                               PointsService pointsService) {
        this.goodsMapper = goodsMapper;
        this.recordMapper = recordMapper;
        this.pointsService = pointsService;
    }

    @Override
    @Transactional
    public void exchange(Long userId, ExchangeDTO dto) {
        ExchangeGoods goods = goodsMapper.selectById(dto.getGoodsId());
        if (goods == null) {
            throw new BizException("商品不存在");
        }
        if (goods.getStatus() != 1) {
            throw new BizException("商品已下架");
        }
        if (goods.getStock() < dto.getQuantity()) {
            throw new BizException("库存不足，当前库存: " + goods.getStock());
        }

        int totalCost = goods.getPointsCost() * dto.getQuantity();

        // 乐观锁扣减库存
        goods.setStock(goods.getStock() - dto.getQuantity());
        int rows = goodsMapper.updateById(goods);
        if (rows == 0) {
            throw new BizException("库存扣减失败，请重试");
        }

        // 扣减积分
        pointsService.deductPoints(userId, totalCost, PointsChangeType.EXCHANGE_DEDUCT.code,
                "兑换商品: " + goods.getName() + " x" + dto.getQuantity(), null);

        // 保存兑换记录
        ExchangeRecord record = new ExchangeRecord();
        record.setUserId(userId);
        record.setGoodsId(goods.getId());
        record.setGoodsName(goods.getName());
        record.setPointsCost(totalCost);
        record.setStatus(1);
        recordMapper.insert(record);

        log.info("用户[{}]兑换商品[{} x{}]，消耗积分{}", userId, goods.getName(), dto.getQuantity(), totalCost);
    }

    @Override
    public Page<ExchangeRecord> getRecords(Long userId, int pageNum, int pageSize) {
        Page<ExchangeRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ExchangeRecord> wrapper = new LambdaQueryWrapper<ExchangeRecord>()
                .eq(ExchangeRecord::getUserId, userId)
                .orderByDesc(ExchangeRecord::getCreatedAt);
        return recordMapper.selectPage(page, wrapper);
    }

    @Override
    public Page<ExchangeGoods> listGoods(int pageNum, int pageSize) {
        Page<ExchangeGoods> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ExchangeGoods> wrapper = new LambdaQueryWrapper<ExchangeGoods>()
                .eq(ExchangeGoods::getStatus, 1)
                .gt(ExchangeGoods::getStock, 0)
                .orderByAsc(ExchangeGoods::getPointsCost);
        return goodsMapper.selectPage(page, wrapper);
    }
}
