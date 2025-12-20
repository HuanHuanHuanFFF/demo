package com.hf.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hf.demo.domain.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("SELECT r.permission FROM sys_user u JOIN user_role ur ON ur.user_id = u.id JOIN sys_role r ON ur.role_id = r.id WHERE u.username = #{username}")
    List<String> getPermissionsByUsername(@Param("username") String username);
}