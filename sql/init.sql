-- ============================================================
-- 薅羊毛信息社区管理系统 - 数据库建表脚本
-- MySQL 5.7+ / 8.0
-- ============================================================

CREATE DATABASE IF NOT EXISTS wool_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE wool_db;

-- 1. 用户表
CREATE TABLE `t_user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `openid`      VARCHAR(64)  NOT NULL COMMENT '微信openid',
    `nickname`    VARCHAR(64)  DEFAULT '' COMMENT '昵称',
    `avatar_url`  VARCHAR(512) DEFAULT '' COMMENT '头像URL',
    `role`        TINYINT      NOT NULL DEFAULT 0 COMMENT '角色: 0=普通用户, 1=管理员',
    `points`      INT          NOT NULL DEFAULT 0 COMMENT '积分余额',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0=禁用, 1=正常',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_openid` (`openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 羊毛信息表
CREATE TABLE `t_wool_info` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     BIGINT        NOT NULL COMMENT '发布者ID',
    `title`       VARCHAR(128)  NOT NULL COMMENT '标题',
    `content`     TEXT          NOT NULL COMMENT '详细内容',
    `category`    VARCHAR(32)   DEFAULT '' COMMENT '分类',
    `source_url`  VARCHAR(512)  DEFAULT '' COMMENT '来源链接',
    `claim_steps` TEXT          DEFAULT NULL COMMENT '领取步骤',
    `status`      TINYINT       NOT NULL DEFAULT 0 COMMENT '状态: 0=待审核, 1=已上线, 2=审核驳回, 3=已下线',
    `reject_reason` VARCHAR(256) DEFAULT '' COMMENT '驳回理由',
    `view_count`  INT           NOT NULL DEFAULT 0 COMMENT '浏览次数',
    `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='羊毛信息表';

-- 3. 积分变动记录表
CREATE TABLE `t_points_log` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     BIGINT       NOT NULL COMMENT '用户ID',
    `change_type` TINYINT      NOT NULL COMMENT '变动类型: 1=发布奖励, 2=兑换扣减, 3=管理员调整',
    `change_value` INT         NOT NULL COMMENT '变动值(正数增加, 负数减少)',
    `before_points` INT        NOT NULL COMMENT '变动前积分',
    `after_points`  INT        NOT NULL COMMENT '变动后积分',
    `remark`      VARCHAR(128) DEFAULT '' COMMENT '备注',
    `biz_id`      BIGINT       DEFAULT NULL COMMENT '关联业务ID(信息ID/兑换记录ID)',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分变动记录表';

-- 4. 兑换商品表
CREATE TABLE `t_exchange_goods` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`          VARCHAR(128)  NOT NULL COMMENT '商品名称',
    `description`   VARCHAR(512)  DEFAULT '' COMMENT '商品描述',
    `image_url`     VARCHAR(512)  DEFAULT '' COMMENT '商品图片',
    `points_cost`   INT           NOT NULL COMMENT '兑换所需积分',
    `stock`         INT           NOT NULL DEFAULT 0 COMMENT '库存数量',
    `status`        TINYINT       NOT NULL DEFAULT 1 COMMENT '状态: 0=下架, 1=上架',
    `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='兑换商品表';

-- 5. 兑换记录表
CREATE TABLE `t_exchange_record` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     BIGINT        NOT NULL COMMENT '用户ID',
    `goods_id`    BIGINT        NOT NULL COMMENT '商品ID',
    `goods_name`  VARCHAR(128)  NOT NULL COMMENT '商品名称(冗余)',
    `points_cost` INT           NOT NULL COMMENT '消耗积分',
    `status`      TINYINT       NOT NULL DEFAULT 1 COMMENT '状态: 0=已取消, 1=已兑换',
    `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='兑换记录表';

-- 初始管理员账号(需手动设置openid)
-- INSERT INTO t_user (openid, nickname, role) VALUES ('admin_openid', '管理员', 1);

-- 示例兑换商品
INSERT INTO t_exchange_goods (name, description, points_cost, stock, status) VALUES
('5元话费券', '满10元可用话费券', 10, 100, 1),
('10元京东E卡', '京东购物卡', 20, 50, 1),
('视频会员月卡', '爱奇艺/优酷月卡任选', 30, 30, 1);
