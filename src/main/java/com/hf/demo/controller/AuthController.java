package com.hf.demo.controller;

import com.hf.demo.domain.SysUser;
import com.hf.demo.domain.vo.Result;
import com.hf.demo.domain.vo.TokenVO;
import com.hf.demo.service.AuthService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public Result<TokenVO> login(@RequestBody SysUser user) {
        TokenVO token = authService.login(user.getUsername(), user.getPassword());
        return Result.ok(token);
    }

    @PostMapping("/refresh")
    public Result<TokenVO> refresh(@RequestBody TokenVO vo) {
        return Result.ok(authService.refresh(vo.getRefreshToken()));
    }

    @PostMapping("/logout")
    public Result<String> logout(@RequestBody TokenVO vo) {
        authService.logout(vo.getRefreshToken());
        return Result.ok("ok");
    }
}