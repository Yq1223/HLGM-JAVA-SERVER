package com.wool.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wool.common.Constants;
import com.wool.common.R;
import com.wool.dto.AuditDTO;
import com.wool.entity.AdminSubscribe;
import com.wool.entity.User;
import com.wool.mapper.AdminSubscribeMapper;
import com.wool.mapper.UserMapper;
import com.wool.service.WoolInfoService;
import com.wool.vo.WoolInfoVO;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;

/**
 * 管理员接口 (所有接口需管理员权限)
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final WoolInfoService woolInfoService;
    private final AdminSubscribeMapper adminSubscribeMapper;
    private final UserMapper userMapper;

    public AdminController(WoolInfoService woolInfoService, AdminSubscribeMapper adminSubscribeMapper, UserMapper userMapper) {
        this.woolInfoService = woolInfoService;
        this.adminSubscribeMapper = adminSubscribeMapper;
        this.userMapper = userMapper;
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

    /**
     * 查询当前管理员的订阅状态
     * GET /api/admin/subscribe/status
     */
    @GetMapping("/subscribe/status")
    public R<?> subscribeStatus(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(Constants.ATTR_USER_ID);
        User user = userMapper.selectById(userId);
        if (user == null) {
            return R.fail("用户不存在");
        }
        String openId = user.getOpenid();
        LambdaQueryWrapper<AdminSubscribe> wrapper = new LambdaQueryWrapper<AdminSubscribe>()
                .eq(AdminSubscribe::getOpenid, openId)
                .eq(AdminSubscribe::getSubscribed, 1);
        boolean subscribed = adminSubscribeMapper.selectCount(wrapper) > 0;
        return R.ok(Map.of("subscribed", subscribed));
    }

    /**
     * 切换订阅状态（开启/关闭）
     * POST /api/admin/subscribe/toggle
     */
    @PostMapping("/subscribe/toggle")
    public R<?> subscribeToggle(@RequestBody Map<String, String> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(Constants.ATTR_USER_ID);
        User user = userMapper.selectById(userId);
        if (user == null) {
            return R.fail("用户不存在");
        }
        String openId = user.getOpenid();
        String templateId = body.get("templateId");

        LambdaQueryWrapper<AdminSubscribe> wrapper = new LambdaQueryWrapper<AdminSubscribe>()
                .eq(AdminSubscribe::getOpenid, openId)
                .eq(AdminSubscribe::getTemplateId, templateId);
        AdminSubscribe existing = adminSubscribeMapper.selectOne(wrapper);

        if (existing != null) {
            // 切换状态
            existing.setSubscribed(existing.getSubscribed() == 1 ? 0 : 1);
            adminSubscribeMapper.updateById(existing);
            return R.ok(Map.of("subscribed", existing.getSubscribed() == 1));
        } else {
            // 首次订阅
            AdminSubscribe subscribe = new AdminSubscribe();
            subscribe.setOpenid(openId);
            subscribe.setTemplateId(templateId);
            subscribe.setSubscribed(1);
            adminSubscribeMapper.insert(subscribe);
            return R.ok(Map.of("subscribed", true));
        }
    }

    /**
     * 管理员订阅审核通知（保留兼容）
     * POST /api/admin/subscribe
     */
    @PostMapping("/subscribe")
    public R<?> subscribe(@RequestBody Map<String, String> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(Constants.ATTR_USER_ID);
        User user = userMapper.selectById(userId);
        if (user == null) {
            return R.fail("用户不存在");
        }
        String openId = user.getOpenid();
        String templateId = body.get("templateId");

        LambdaQueryWrapper<AdminSubscribe> wrapper = new LambdaQueryWrapper<AdminSubscribe>()
                .eq(AdminSubscribe::getOpenid, openId)
                .eq(AdminSubscribe::getTemplateId, templateId);
        AdminSubscribe existing = adminSubscribeMapper.selectOne(wrapper);

        if (existing != null) {
            existing.setSubscribed(1);
            adminSubscribeMapper.updateById(existing);
        } else {
            AdminSubscribe subscribe = new AdminSubscribe();
            subscribe.setOpenid(openId);
            subscribe.setTemplateId(templateId);
            subscribe.setSubscribed(1);
            adminSubscribeMapper.insert(subscribe);
        }
        return R.ok();
    }
}
