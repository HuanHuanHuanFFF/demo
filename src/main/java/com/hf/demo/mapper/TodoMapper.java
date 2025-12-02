package com.hf.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hf.demo.domain.dto.Todo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TodoMapper extends BaseMapper<Todo> {

}
