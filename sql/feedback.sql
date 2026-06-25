-- 反馈表
CREATE TABLE IF NOT EXISTS `t_feedback` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `title` VARCHAR(128) NOT NULL COMMENT '反馈标题',
    `content` TEXT NOT NULL COMMENT '反馈内容',
    `status` INT NOT NULL DEFAULT 0 COMMENT '状态：0-待处理，1-进行中，2-已完成',
    `reply` TEXT COMMENT '管理员回复',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户反馈表';
