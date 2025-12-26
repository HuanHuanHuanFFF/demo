package com.hf.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hf.demo.domain.SysUser;
import com.hf.demo.domain.vo.CodeStatus;
import com.hf.demo.domain.vo.TokenVO;
import com.hf.demo.exception.BizException;
import com.hf.demo.infra.redis.RedisSessionStore;
import com.hf.demo.mapper.SysUserMapper;
import com.hf.demo.security.guard.LoginFailGuard;
import com.hf.demo.security.jwt.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
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
    private LoginFailGuard loginFailGuard;

    @Resource
    private JwtUtils jwtUtils;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedisSessionStore redisSessionStore;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Value("${jwt.refresh-key-prefix}")
    private String refreshKeyPrefix;

    private static final DefaultRedisScript<Long> REFRESH_CONSUME_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('EXISTS', KEYS[1]) == 1 then " +
                    "  redis.call('DEL', KEYS[1]); " +
                    "  return 1; " +
                    "else " +
                    "  return 0; " +
                    "end",
            Long.class
    );

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

    public TokenVO login(String sid, String username, String password) {
        // 这一步会自动调用 UserDetailsServiceImpl.loadUserByUsername
        // 如果密码不对，这里会直接抛出 BadCredentialsException
        if (loginFailGuard.isLocked(username)) throw new BizException(CodeStatus.LOGIN_FAILED);

        Authentication authenticate;
        try {
            authenticate = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
        } catch (AuthenticationException e) {
            Long failCnt = loginFailGuard.onFail(username);
            throw new BizException(CodeStatus.LOGIN_FAILED);
        }

        // 储存会话到Redis
        redisSessionStore.upsertOnLogin(username, sid, System.currentTimeMillis());

        // 消除失败次数
        loginFailGuard.onSuccess(username);

        // 设置token权限,签发token
        List<String> authCodes = authenticate
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return generateTokenVO(authenticate.getName(), authCodes, sid);
    }

    public TokenVO refresh(String sid, String refreshToken) {
        // 验证token
        if (!jwtUtils.validateToken(refreshToken))
            throw new BizException(CodeStatus.UNAUTHORIZED, "refresh无效");
        // 解析token
        Claims claims = jwtUtils.parseClaims(refreshToken);
        String username = jwtUtils.extractUsername(claims);
        String refreshId = jwtUtils.extractRefreshId(claims);

        // 查询refreshID
        String key = refreshKeyPrefix + refreshId + ":" + sid;
        Long consumed = stringRedisTemplate.execute(
                REFRESH_CONSUME_SCRIPT,
                Collections.singletonList(key)
        );
        if (consumed == null || consumed != 1L)
            throw new BizException(CodeStatus.UNAUTHORIZED, "refresh无效");

        // 更新会话
        redisSessionStore.touchOnRefresh(username, sid, System.currentTimeMillis());

        // 更新权限,签发token
        List<String> authCodes = loadAuthCodes(username);
        return generateTokenVO(username, authCodes, sid);
    }

    private TokenVO generateTokenVO(String username, List<String> authCodes, String sid) {
        String newAccessToken = jwtUtils.generateAccessToken(username, authCodes);
        String newRefreshId = UUID.randomUUID().toString();
        String newRefreshToken = jwtUtils.generateRefreshToken(username, newRefreshId);
        stringRedisTemplate.opsForValue().set(
                refreshKeyPrefix + newRefreshId + ":" + sid,
                username,
                refreshExpirationMs,
                TimeUnit.MILLISECONDS
        );
        return new TokenVO(newAccessToken, newRefreshToken);
    }

    public void logout(String sid, String refreshToken) {
        if (!jwtUtils.validateToken(refreshToken)) return;

        // 解析token
        Claims claims = jwtUtils.parseClaims(refreshToken);
        String username = jwtUtils.extractUsername(claims);
        String refreshId = jwtUtils.extractRefreshId(claims);

        // 查询refreshID
        String key = refreshKeyPrefix + refreshId + ":" + sid;
        Long consumed = stringRedisTemplate.execute(
                REFRESH_CONSUME_SCRIPT,
                Collections.singletonList(key)
        );
        if (consumed == null || consumed != 1L)
            return;

        redisSessionStore.deleteOnLogout(username, sid);
    }

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
}