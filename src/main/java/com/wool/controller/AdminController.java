package com.wool.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wool.common.Constants;
import com.wool.common.R;
import com.wool.dto.AuditDTO;
import com.wool.service.WoolInfoService;
import com.wool.vo.WoolInfoVO;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 管理员接口 (所有接口需管理员权限)
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final WoolInfoService woolInfoService;

    public AdminController(WoolInfoService woolInfoService) {
        this.woolInfoService = woolInfoService;
    }

    /**
     * 管理员查询信息列表(所有状态)
     * GET /api/admin/wool/list?pageNum=1&pageSize=10&status=0&keyword=xxx
     */
    @GetMapping("/wool/list")
    public R<Page<WoolInfoVO>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        Page<WoolInfoVO> page = woolInfoService.adminList(pageNum, pageSize, status, keyword);
        return R.ok(page);
    }

    /**
     * 审核信息
     * POST /api/admin/wool/audit/{id}
     */
    @PostMapping("/wool/audit/{id}")
    public R<?> audit(@PathVariable Long id, @Valid @RequestBody AuditDTO dto, HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute(Constants.ATTR_USER_ID);
        woolInfoService.audit(id, dto, adminId);
        return R.ok();
    }

    /**
     * 上线信息
     * PUT /api/admin/wool/online/{id}
     */
    @PutMapping("/wool/online/{id}")
    public R<?> online(@PathVariable Long id, HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute(Constants.ATTR_USER_ID);
        woolInfoService.toggleOnline(id, true, adminId);
        return R.ok();
    }

    /**
     * 下线信息
     * PUT /api/admin/wool/offline/{id}
     */
    @PutMapping("/wool/offline/{id}")
    public R<?> offline(@PathVariable Long id, HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute(Constants.ATTR_USER_ID);
        woolInfoService.toggleOnline(id, false, adminId);
        return R.ok();
    }

    /**
     * 删除任意信息
     * DELETE /api/admin/wool/delete/{id}
     */
    @DeleteMapping("/wool/delete/{id}")
    public R<?> delete(@PathVariable Long id, HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute(Constants.ATTR_USER_ID);
        woolInfoService.adminDelete(id, adminId);
        return R.ok();
    }
}
