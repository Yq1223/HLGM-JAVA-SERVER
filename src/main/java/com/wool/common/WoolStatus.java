package com.wool.common;

/**
 * 羊毛信息状态枚举
 */
public enum WoolStatus {
    PENDING(0, "待审核"),
    ONLINE(1, "已上线"),
    REJECTED(2, "审核驳回"),
    OFFLINE(3, "已下线");

    public final int code;
    public final String desc;

    WoolStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static WoolStatus of(int code) {
        for (WoolStatus s : values()) {
            if (s.code == code) return s;
        }
        return null;
    }
}
