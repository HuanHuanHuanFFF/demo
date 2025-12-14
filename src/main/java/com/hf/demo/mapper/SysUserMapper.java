package com.hf.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hf.demo.domain.SysUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}