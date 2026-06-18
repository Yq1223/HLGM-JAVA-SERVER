package com.wool.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wool.common.Constants;
import com.wool.common.R;
import com.wool.service.PointsService;
import com.wool.vo.PointsLogVO;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/points")
public class PointsController {

    private final PointsService pointsService;

    public PointsController(PointsService pointsService) {
        this.pointsService = pointsService;
    }

    /**
     * 查询当前用户积分
     * GET /api/points/balance
     */
    @GetMapping("/balance")
    public R<Integer> balance(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(Constants.ATTR_USER_ID);
        Integer points = pointsService.getPoints(userId);
        return R.ok(points);
    }

    /**
     * 查询积分变动记录
     * GET /api/points/log?pageNum=1&pageSize=10
     */
    @GetMapping("/log")
    public R<Page<PointsLogVO>> log(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(Constants.ATTR_USER_ID);
        Page<PointsLogVO> page = pointsService.getPointsLog(userId, pageNum, pageSize);
        return R.ok(page);
    }
}
