package com.hf.demo.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class Todo {
    Long id;
    String title;
    @DateTimeFormat
    LocalDateTime createdTime;
}
