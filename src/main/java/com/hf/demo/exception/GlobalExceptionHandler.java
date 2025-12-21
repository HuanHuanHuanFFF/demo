package com.hf.demo.exception;

import com.hf.demo.domain.vo.CodeStatus;
import com.hf.demo.domain.vo.Result;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BizException.class)
    public Result<String> handleBiz(BizException e) {
        return Result.fail(e.getCodeStatus(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = fieldError != null ? fieldError.getDefaultMessage() : "请求参数不合法";
        log.error(msg);
        return Result.fail(CodeStatus.PARAM_ERROR, msg);
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public Result<String> handleConstraintViolation(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse("请求参数不合法");
        log.error(msg);
        return Result.fail(CodeStatus.PARAM_ERROR, msg);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<String> handleMissingParam(MissingServletRequestParameterException e) {
        String msg = e.getParameterName() + " 参数必填";
        log.error(msg);
        return Result.fail(CodeStatus.PARAM_ERROR, msg);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<String> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.error("参数类型错误: {}", e.getMessage());
        return Result.fail(CodeStatus.PARAM_ERROR, "请求参数不合法");
    }

    @ExceptionHandler({BadCredentialsException.class, InternalAuthenticationServiceException.class})
    public Result<String> handleAuthException(Exception e) {
        log.error("认证失败: {}", e.getMessage());
        return Result.fail(CodeStatus.LOGIN_FAILED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Result<String> handleAccessDeniedException(AccessDeniedException e) {
        log.error("权限不足: {}", e.getMessage());
        // 如果你的 CodeStatus 里有 NO_PERMISSION 最好，没有就先用 SERVER_ERROR 或自定义一个
        return Result.fail(CodeStatus.SERVER_ERROR, "权限不足，请联系管理员");
    }

    @ExceptionHandler({RedisConnectionFailureException.class, RedisSystemException.class})
    public Result<String> handleRedisDown(org.springframework.data.redis.RedisConnectionFailureException e) {
        log.error("[REDIS][DOWN] {}", e.getMessage());
        return Result.fail(CodeStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("unknown error", e);
        return Result.fail(CodeStatus.SERVER_ERROR);
    }
}
