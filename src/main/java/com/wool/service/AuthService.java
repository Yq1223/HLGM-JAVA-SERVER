package com.wool.service;

import com.wool.dto.WxLoginDTO;
import com.wool.dto.WxRegisterDTO;
import com.wool.vo.LoginVO;

public interface AuthService {

    /**
     * 微信登录（已注册用户直接登录，新用户返回 needRegister=true）
     */
    LoginVO wxLogin(WxLoginDTO dto);

    /**
     * 新用户注册
     */
    LoginVO wxRegister(WxRegisterDTO dto);
}
