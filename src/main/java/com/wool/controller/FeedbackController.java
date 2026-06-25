package com.wool.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wool.common.Constants;
import com.wool.common.R;
import com.wool.dto.FeedbackAuditDTO;
import com.wool.dto.FeedbackDTO;
import com.wool.service.FeedbackService;
import com.wool.vo.FeedbackVO;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    /**
     * 提交反馈
     * POST /api/feedback/submit
     */
    @PostMapping("/submit")
    public R<Long> submit(@Valid @RequestBody FeedbackDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(Constants.ATTR_USER_ID);
        Long id = feedbackService.submit(dto, userId);
        return R.ok(id);
    }

    /**
     * 查询我的反馈列表
     * GET /api/feedback/mine?pageNum=1&pageSize=10&status=0
     */
    @GetMapping("/mine")
    public R<Page<FeedbackVO>> mine(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(Constants.ATTR_USER_ID);
        Page<FeedbackVO> page = feedbackService.myList(userId, pageNum, pageSize, status);
        return R.ok(page);
    }

    /**
     * 管理员查询所有反馈列表
     * GET /api/feedback/admin/list?pageNum=1&pageSize=10&status=0
     */
    @GetMapping("/admin/list")
    public R<Page<FeedbackVO>> adminList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute(Constants.ATTR_USER_ROLE);
        if (role == null || role != Constants.ROLE_ADMIN) {
            return R.fail("无权限");
        }
        Page<FeedbackVO> page = feedbackService.adminList(pageNum, pageSize, status);
        return R.ok(page);
    }

    /**
     * 获取反馈详情
     * GET /api/feedback/detail/{id}
     */
    @GetMapping("/detail/{id}")
    public R<FeedbackVO> detail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(Constants.ATTR_USER_ID);
        Integer role = (Integer) request.getAttribute(Constants.ATTR_USER_ROLE);
        FeedbackVO vo = feedbackService.getDetail(id, userId, role);
        return R.ok(vo);
    }

    /**
     * 管理员处理反馈
     * PUT /api/feedback/audit/{id}
     */
    @PutMapping("/audit/{id}")
    public R<?> audit(@PathVariable Long id, @Valid @RequestBody FeedbackAuditDTO dto, HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute(Constants.ATTR_USER_ROLE);
        if (role == null || role != Constants.ROLE_ADMIN) {
            return R.fail("无权限");
        }
        Long adminId = (Long) request.getAttribute(Constants.ATTR_USER_ID);
        feedbackService.audit(id, dto, adminId);
        return R.ok();
    }
}
