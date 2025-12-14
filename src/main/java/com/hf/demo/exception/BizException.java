package com.hf.demo.exception;

import com.hf.demo.domain.vo.CodeStatus;
import lombok.Getter;

@Getter
public class BizException extends RuntimeException {
    private final CodeStatus codeStatus;
    private final String msg;

    public BizException(CodeStatus codeStatus) {
        super(codeStatus.getMsg());
        this.msg = codeStatus.getMsg();
        this.codeStatus = codeStatus;
    }

    public BizException(CodeStatus codeStatus, String msg) {
        super(msg);
        this.msg = msg;
        this.codeStatus = codeStatus;
    }
}
