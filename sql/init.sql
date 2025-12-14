-- 使用 todo_db 库（防止建错地方）
SET CHARSET utf8mb4;
USE `todo_db`;

DROP TABLE IF EXISTS `todo`;
CREATE TABLE `todo`
(
    `id`           bigint       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `title`        varchar(255) NOT NULL COMMENT '待办事项标题',
    `created_time` datetime     NOT NULL COMMENT '创建时间',
    `version`      int          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='待办事项表';

-- 修正后的插入语句
INSERT INTO `todo` (`title`, `created_time`, `version`)
VALUES ('Docker 一键部署测试', NOW(), 1);

-- ----------------------------
-- User Table for Spring Security
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;

CREATE TABLE `sys_user`
(
    `id`       bigint       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` varchar(50)  NOT NULL COMMENT '用户名',
    `password` varchar(100) NOT NULL COMMENT '密码(加密存)',
    `role`     varchar(50) DEFAULT 'USER' COMMENT '角色',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='系统用户表';
