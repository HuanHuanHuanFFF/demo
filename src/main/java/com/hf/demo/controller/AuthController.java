package com.hf.demo.controller;

import com.hf.demo.domain.SysUser;
import com.hf.demo.domain.vo.CodeStatus;
import com.hf.demo.domain.vo.Result;
import com.hf.demo.domain.vo.TokenVO;
import com.hf.demo.exception.BizException;
import com.hf.demo.service.AuthService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Resource
    private AuthService authService;

    @PostMapping("/register")
    public Result<String> register(@RequestBody SysUser user) {
        authService.register(user.getUsername(), user.getPassword());
        return Result.ok("注册成功");
    }

    @PostMapping("/login")
    public Result<TokenVO> login(HttpServletResponse response, @RequestHeader(value = "X-Device-Id", required = false) String sid, @RequestBody SysUser user) {
        if (sid == null || sid.isBlank())
            sid = UUID.randomUUID().toString();
        TokenVO token = authService.login(sid, user.getUsername(), user.getPassword());
        response.setHeader("X-Device-Id", sid);
        return Result.ok(token);
    }

    @PostMapping("/refresh")
    public Result<TokenVO> refresh(HttpServletResponse response, @RequestHeader(value = "X-Device-Id", required = false) String sid, @RequestBody TokenVO vo) {
        if (sid == null || sid.isBlank()) throw new BizException(CodeStatus.UNAUTHORIZED);
        response.setHeader("X-Device-Id", sid);
        return Result.ok(authService.refresh(sid, vo.getRefreshToken()));
    }

    @PostMapping("/logout")
    public Result<String> logout(HttpServletResponse response, @RequestHeader(value = "X-Device-Id", required = false) String sid, @RequestBody TokenVO vo) {
        if (sid == null || sid.isBlank()) throw new BizException(CodeStatus.UNAUTHORIZED);
        authService.logout(sid, vo.getRefreshToken());
        response.setHeader("X-Device-Id", sid);
        return Result.ok("ok");
    }
}