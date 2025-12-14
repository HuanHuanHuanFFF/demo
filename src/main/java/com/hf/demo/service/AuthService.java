package com.hf.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hf.demo.domain.SysUser;
import com.hf.demo.domain.vo.CodeStatus;
import com.hf.demo.exception.BizException;
import com.hf.demo.mapper.SysUserMapper;
import com.hf.demo.util.JwtUtils;
import jakarta.annotation.Resource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Resource
    private SysUserMapper userMapper;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    private JwtUtils jwtUtils;

    public void register(String username, String password) {
        // 1. 检查用户名是否已存在 (防止重复注册报错)
        QueryWrapper<SysUser> query = new QueryWrapper<>();
        query.eq("username", username);
        if (userMapper.selectCount(query) > 0) {
            throw new BizException(CodeStatus.PARAM_ERROR, "用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(username);

        String encodedPassword = passwordEncoder.encode(password);

        user.setPassword(encodedPassword);
        user.setRole("USER");
        userMapper.insert(user);
    }

    public String login(String username, String password) {
        // 1. 用户验证
        // 这一步会自动调用刚才写的 UserDetailsServiceImpl.loadUserByUsername
        // 如果密码不对，这里会直接抛出 BadCredentialsException
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        // 2. 验证通过，生成 Token
        return jwtUtils.generateToken(username);
    }
}