package com.wool.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wool.common.Constants;
import com.wool.common.R;
import com.wool.dto.ExchangeDTO;
import com.wool.entity.ExchangeGoods;
import com.wool.entity.ExchangeRecord;
import com.wool.service.ExchangeService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/exchange")
public class ExchangeController {

    private final ExchangeService exchangeService;

    public ExchangeController(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }

    /**
     * 查询可兑换商品列表(公开)
     * GET /api/exchange/goods?pageNum=1&pageSize=10
     */
    @GetMapping("/goods")
    public R<Page<ExchangeGoods>> goods(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<ExchangeGoods> page = exchangeService.listGoods(pageNum, pageSize);
        return R.ok(page);
    }

    /**
     * 兑换商品
     * POST /api/exchange/do
     */
    @PostMapping("/do")
    public R<?> exchange(@Valid @RequestBody ExchangeDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(Constants.ATTR_USER_ID);
        exchangeService.exchange(userId, dto);
        return R.ok();
    }

    /**
     * 查询我的兑换记录
     * GET /api/exchange/records?pageNum=1&pageSize=10
     */
    @GetMapping("/records")
    public R<Page<ExchangeRecord>> records(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(Constants.ATTR_USER_ID);
        Page<ExchangeRecord> page = exchangeService.getRecords(userId, pageNum, pageSize);
        return R.ok(page);
    }
}
