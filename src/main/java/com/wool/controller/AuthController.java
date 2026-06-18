package com.wool.controller;

import com.wool.common.R;
import com.wool.dto.WxLoginDTO;
import com.wool.service.AuthService;
import com.wool.vo.LoginVO;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
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
}
