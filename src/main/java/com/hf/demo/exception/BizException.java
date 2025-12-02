package com.hf.demo.exception;

import com.hf.demo.domain.vo.CodeStatus;
import lombok.Getter;

@Getter
public class BizException extends RuntimeException {
    private final CodeStatus codeStatus;

    public BizException(CodeStatus codeStatus) {
        super(codeStatus.getMsg());
        this.codeStatus = codeStatus;
    }

    public BizException(CodeStatus codeStatus, String msg) {
        super(msg);
        this.codeStatus = codeStatus;
    }
}
