package com.wool.service;

import com.wool.dto.WxLoginDTO;
import com.wool.vo.LoginVO;

public interface AuthService {

    /**
     * 微信登录
     */
    LoginVO wxLogin(WxLoginDTO dto);
}
