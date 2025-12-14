package com.hf.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hf.demo.domain.SysUser;
import com.hf.demo.mapper.SysUserMapper;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

import java.util.Collections;

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

        // 3. 查到了，封装成 Spring Security 需要的 User 对象
        // 参数分别是：用户名、数据库里的密文密码、权限列表(暂时为空)
        return new User(
                sysUser.getUsername(),
                sysUser.getPassword(),
                Collections.emptyList() // 暂时不处理复杂的角色权限
        );
    }
}