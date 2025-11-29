package com.hf.demo.common;

import lombok.Getter;

@Getter
public enum CodeStatus {
    SUCCESS(200, "ok"),
    PARAM_ERROR(1001, "参数错误"),
    NOT_FOUND(1004, "数据不存在"),
    SERVER_ERROR(500, "服务内部错误");

    private final int code;
    private final String msg;

    CodeStatus(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
