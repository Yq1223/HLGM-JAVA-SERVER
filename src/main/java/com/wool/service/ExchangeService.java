package com.wool.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wool.dto.ExchangeDTO;
import com.wool.entity.ExchangeGoods;
import com.wool.entity.ExchangeRecord;

public interface ExchangeService {

    /**
     * 兑换商品
     */
    void exchange(Long userId, ExchangeDTO dto);

    /**
     * 查询兑换记录
     */
    Page<ExchangeRecord> getRecords(Long userId, int pageNum, int pageSize);

    /**
     * 查询商品列表
     */
    Page<ExchangeGoods> listGoods(int pageNum, int pageSize);
}
