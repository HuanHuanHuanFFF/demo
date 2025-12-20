package com.hf.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hf.demo.domain.SysUser;
import com.hf.demo.domain.vo.CodeStatus;
import com.hf.demo.domain.vo.TokenVO;
import com.hf.demo.exception.BizException;
import com.hf.demo.mapper.SysUserMapper;
import com.hf.demo.util.JwtUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Value("${jwt.refresh-key-prefix}")
    private String refreshKeyPrefix;

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
        userMapper.insert(user);
    }

    public TokenVO login(String username, String password) {
        // 1. 用户验证
        // 这一步会自动调用刚才写的 UserDetailsServiceImpl.loadUserByUsername
        // 如果密码不对，这里会直接抛出 BadCredentialsException
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        List<String> authCodes = authenticate.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        return generateTokenVO(authenticate.getName(), authCodes);
    }


    public TokenVO refresh(String refreshToken) {
        if (!jwtUtils.validateToken(refreshToken))
            throw new BizException(CodeStatus.UNAUTHORIZED, "refresh无效");

        String username = jwtUtils.extractUsername(refreshToken);
        String refreshId = jwtUtils.extractRefreshId(refreshToken);

        String key = refreshKeyPrefix + refreshId;

        Long consumed = stringRedisTemplate.execute(
                REFRESH_CONSUME_SCRIPT,
                Collections.singletonList(key)
        );
        if (consumed == null || consumed != 1L)
            throw new BizException(CodeStatus.UNAUTHORIZED, "refresh无效");

        List<String> authCodes = loadAuthCodes(username);

        return generateTokenVO(username, authCodes);
    }

    private TokenVO generateTokenVO(String username, List<String> authCodes) {
        String newAccessToken = jwtUtils.generateAccessToken(username, authCodes);
        String newRefreshId = UUID.randomUUID().toString();
        String newRefreshToken = jwtUtils.generateRefreshToken(username, newRefreshId);
        stringRedisTemplate.opsForValue().set(
                refreshKeyPrefix + newRefreshId,
                username,
                refreshExpirationMs,
                TimeUnit.MILLISECONDS
        );
        return new TokenVO(newAccessToken, newRefreshToken);
    }

    private static final DefaultRedisScript<Long> REFRESH_CONSUME_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('EXISTS', KEYS[1]) == 1 then " +
                    "  redis.call('DEL', KEYS[1]); " +
                    "  return 1; " +
                    "else " +
                    "  return 0; " +
                    "end",
            Long.class
    );

    private List<String> loadAuthCodes(String username) {
        List<String> perms = userMapper.getPermissionsByUsername(username);
        if (perms == null || perms.isEmpty()) return List.of();

        return perms.stream()
                .filter(s -> s != null && !s.isBlank())
                .flatMap(s -> Arrays.stream(s.split(",")))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList();
    }

    public void logout(String refreshToken) {
        if (!jwtUtils.validateToken(refreshToken)) return;
        String refreshId = jwtUtils.extractRefreshId(refreshToken);
        stringRedisTemplate.delete(refreshKeyPrefix + refreshId);
    }
}