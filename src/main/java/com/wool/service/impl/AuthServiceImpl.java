package com.wool.service.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wool.common.BizException;
import com.wool.dto.WxLoginDTO;
import com.wool.entity.User;
import com.wool.mapper.UserMapper;
import com.wool.service.AuthService;
import com.wool.util.JwtUtil;
import com.wool.vo.LoginVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Value("${wechat.appid}")
    private String appid;

    @Value("${wechat.secret}")
    private String secret;

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(UserMapper userMapper, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }

    @Override
    @Transactional
    public LoginVO wxLogin(WxLoginDTO dto) {
        // 1. 调用微信接口获取openid
        String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                appid, secret, dto.getCode());

        String resp = HttpUtil.get(url);
        JSONObject json = JSONUtil.parseObj(resp);
        log.info("微信登录响应: {}", resp);

        String openid = json.getStr("openid");
        if (openid == null) {
            throw new BizException("微信登录失败: " + json.getStr("errmsg"));
        }

        // 2. 查找或创建用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getOpenid, openid));

        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setNickname(dto.getNickname() != null ? dto.getNickname() : "微信用户");
            user.setAvatarUrl(dto.getAvatarUrl() != null ? dto.getAvatarUrl() : "");
            user.setRole(0);
            user.setPoints(0);
            user.setStatus(1);
            userMapper.insert(user);
            log.info("新用户注册: openid={}", openid);
        }

        // 3. 生成JWT
        String token = jwtUtil.generateToken(user.getId(), user.getRole());

        // 4. 组装返回
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setRole(user.getRole());
        vo.setPoints(user.getPoints());
        return vo;
    }
}
