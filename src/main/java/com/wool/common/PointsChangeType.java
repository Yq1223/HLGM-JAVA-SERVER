package com.wool.common;

/**
 * 积分变动类型
 */
public enum PointsChangeType {
    PUBLISH_REWARD(1, "发布奖励"),
    EXCHANGE_DEDUCT(2, "兑换扣减"),
    ADMIN_ADJUST(3, "管理员调整");

    public final int code;
    public final String desc;

    PointsChangeType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
