package com.hf.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Todo {
    Long id;
    @NotBlank(message = "标题不能为空")
    @Size(max = 50, message = "标题长度不能超过50字符")
    String title;
    LocalDateTime createdTime;
}
