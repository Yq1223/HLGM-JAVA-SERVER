package com.wool.dto;

import javax.validation.constraints.NotBlank;

public class WxLoginDTO {

    @NotBlank(message = "code不能为空")
    private String code;

    private String nickname;
    private String avatarUrl;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}
