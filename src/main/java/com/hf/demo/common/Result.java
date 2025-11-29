package com.hf.demo.common;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Result<T> {
    private int code;
    private String msg;
    private T data;

    public static <T> Result<T> ok(T data) {
        return Result.<T>builder()
                .code(CodeStatus.SUCCESS.getCode())
                .msg(CodeStatus.SUCCESS.getMsg())
                .data(data)
                .build();
    }

    public static <T> Result<T> fail(CodeStatus codeStatus) {
        return fail(codeStatus, null);
    }

    public static <T> Result<T> fail(CodeStatus codeStatus, T data) {
        return Result.<T>builder()
                .code(codeStatus.getCode())
                .msg(codeStatus.getMsg())
                .data(data)
                .build();
    }
}
