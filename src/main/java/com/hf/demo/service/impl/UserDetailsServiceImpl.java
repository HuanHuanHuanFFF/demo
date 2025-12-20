package com.hf.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hf.demo.domain.SysUser;
import com.hf.demo.mapper.SysUserMapper;
import jakarta.annotation.Resource;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Resource
    private SysUserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 查询数据库
        QueryWrapper<SysUser> query = new QueryWrapper<>();
        query.eq("username", username);
        SysUser sysUser = userMapper.selectOne(query);

        // 2. 如果没查到，必须抛出这个特定的异常，Spring Security 会捕获它
        if (sysUser == null) {
            throw new UsernameNotFoundException("用户名不存在");
        }
        // 3. 继续查权限列表
        List<String> permissions = userMapper.getPermissionsByUsername(username);
        List<SimpleGrantedAuthority> authorities = permissions.stream()
                .filter(s -> s != null && !s.isBlank())
                .flatMap(s -> Arrays.stream(s.split(",")))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .map(SimpleGrantedAuthority::new).toList();

        // 参数分别是：用户名、数据库里的密文密码、权限列表(暂时为空)
        return new User(
                sysUser.getUsername(),
                sysUser.getPassword(),
                authorities
        );
    }
}