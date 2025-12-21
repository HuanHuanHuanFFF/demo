package com.hf.demo.domain.vo;

import lombok.Getter;

@Getter
public enum CodeStatus {
    SUCCESS(0, "ok"),
    PARAM_ERROR(1001, "参数错误"),
    UNAUTHORIZED(1002, "未登录或登录已过期"),
    NO_PERMISSION(1003, "权限不足"),
    NOT_FOUND(1004, "数据不存在"),
    UPDATE_CONFLICT(1005, "数据已被其他请求修改,请刷新后重试"),
    TOO_MANY_REQUESTS(1006, "请求过于频繁，请稍后再试"),
    LOGIN_FAILED(1007, "账号或密码错误"),
    SERVER_ERROR(2000, "服务内部错误");

    private final int code;
    private final String msg;

    CodeStatus(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
