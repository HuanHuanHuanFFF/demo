package com.hf.demo.controller;

import com.hf.demo.dto.Todo;
import com.hf.demo.service.HelloService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("hello")
public class HelloController {
    @Resource
    private HelloService helloService;

    @GetMapping("/huanf")
    public Map<String, String> hello() {
        Map<String, String> result = new HashMap<>();
        result.put("msg", "Hello,HuanF!");
        result.put("author", "HuanF");
        return result;
    }

    @GetMapping("/todos")
    public List<Todo> listTodos() {
        return helloService.listTodos();
    }

    @PostMapping("/add")
    public String add(@RequestBody Todo todo) {
        helloService.addTodo(todo);
        return "success";
    }
}
