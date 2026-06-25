package com.wool.vo;

public class LoginVO {

    private String token;
    private Long userId;
    private String nickname;
    private String avatarUrl;
    private Integer role;
    private Integer points;
    private boolean needRegister;  // 是否需要注册（新用户）

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public Integer getRole() { return role; }
    public void setRole(Integer role) { this.role = role; }

    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }

    public boolean isNeedRegister() { return needRegister; }
    public void setNeedRegister(boolean needRegister) { this.needRegister = needRegister; }
}
