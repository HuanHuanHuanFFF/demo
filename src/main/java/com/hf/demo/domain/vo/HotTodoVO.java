package com.hf.demo.domain.vo;

import com.hf.demo.domain.dto.Todo;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HotTodoVO {
    private Todo todo;
    private long score;
}
