package com.wool.controller;

import com.wool.common.Constants;
import com.wool.common.R;
import com.wool.dto.WxLoginDTO;
import com.wool.entity.User;
import com.wool.mapper.UserMapper;
import com.wool.service.AuthService;
import com.wool.vo.LoginVO;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserMapper userMapper;

    public AuthController(AuthService authService, UserMapper userMapper) {
        this.authService = authService;
        this.userMapper = userMapper;
    }

    /**
     * 微信登录
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public R<LoginVO> login(@Valid @RequestBody WxLoginDTO dto) {
        LoginVO vo = authService.wxLogin(dto);
        return R.ok(vo);
    }

    /**
     * 获取当前登录用户信息
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public R<Map<String, Object>> me(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(Constants.ATTR_USER_ID);
        User user = userMapper.selectById(userId);
        if (user == null) {
            return R.fail("用户不存在");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getId());
        data.put("nickname", user.getNickname());
        data.put("avatarUrl", user.getAvatarUrl());
        data.put("role", user.getRole());
        data.put("points", user.getPoints());
        return R.ok(data);
    }
}
