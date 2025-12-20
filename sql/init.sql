-- ============================
-- RBAC v1 + Todo Demo (Full Init)
-- DB: demo
-- ============================

-- 0) 选库（防止建错地方）
USE `demo`;

-- 1) 清理（必须先删子表，再删父表）
DROP TABLE IF EXISTS `user_role`;
DROP TABLE IF EXISTS `sys_role`;
DROP TABLE IF EXISTS `sys_user`;
DROP TABLE IF EXISTS `todo`;

-- 2) todo 表
CREATE TABLE `todo`
(
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `title`        VARCHAR(255) NOT NULL COMMENT '待办事项标题',
    `created_time` DATETIME     NOT NULL COMMENT '创建时间',
    `version`      INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='待办事项表';

INSERT INTO `todo` (`title`, `created_time`, `version`)
VALUES ('Docker 一键部署测试', NOW(), 1);

-- 3) 用户表（Spring Security 用）
CREATE TABLE `sys_user`
(
    `id`       BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50)  NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码(加密存)',
    `role`     VARCHAR(50) DEFAULT 'USER' COMMENT '历史字段/可保留但不建议用于 RBAC',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='系统用户表';

-- 4) 角色表（RBAC v1：权限集合放在 permission 字符串里）
CREATE TABLE `sys_role`
(
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `role_name`  VARCHAR(64)  NOT NULL COMMENT '角色名',
    `permission` VARCHAR(512) NOT NULL COMMENT 'perm codes, comma-separated. e.g. todo:read,todo:write',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_name` (`role_name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='系统角色表';

-- 5) 用户-角色关联表（外键字段类型必须与父表一致）
CREATE TABLE `user_role`
(
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (`user_id`, `role_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role_id` (`role_id`),
    CONSTRAINT `fk_user_role_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`),
    CONSTRAINT `fk_user_role_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户-角色关联表';

-- 6) 初始化角色 + 权限（v1：admin=当前系统全部权限）
INSERT INTO `sys_role`(`role_name`, `permission`)
VALUES ('TODO_READ', 'todo:read'),
       ('TODO_RW', 'todo:read,todo:write'),
       ('ADMIN', 'todo:read,todo:write');

-- 7) 初始化用户：admin（密码 123456 的 bcrypt，你已给定）
INSERT INTO `sys_user`(`username`, `password`)
VALUES ('admin', '$2a$10$RPsMG4nMjCQHK7KIXFm/ieb.8TZlaUFvweuGFGUtzXi7OthiaeBwq');

-- 8) 绑定：admin -> ADMIN
INSERT INTO `user_role`(`user_id`, `role_id`)
SELECT u.id, r.id
FROM `sys_user` u
         JOIN `sys_role` r
WHERE u.username = 'admin'
  AND r.role_name = 'ADMIN';

-- 9) 自检：确认 admin 的角色与权限能查出来
SELECT u.username, r.role_name, r.permission
FROM `sys_user` u
         JOIN `user_role` ur ON ur.user_id = u.id
         JOIN `sys_role` r ON r.id = ur.role_id
WHERE u.username = 'admin';
