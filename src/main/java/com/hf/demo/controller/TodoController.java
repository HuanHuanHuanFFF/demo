package com.hf.demo.controller;

import com.hf.demo.common.Result;
import com.hf.demo.dto.Todo;
import com.hf.demo.service.TodoService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/todos")
public class TodoController {
    @Resource
    private TodoService todoService;

    @GetMapping("/hello")
    public Result<Map<String, String>> hello() {
        Map<String, String> map = new HashMap<>();
        map.put("msg", "Hello,HuanF!");
        map.put("author", "HuanF");
        return Result.ok(map);
    }

    @GetMapping
    public Result<List<Todo>> listTodos() {
        return Result.ok(todoService.listTodos());
    }

    @PostMapping
    public Result<String> addTodo(@Validated @RequestBody Todo todo) {
        todoService.addTodo(todo);
        return Result.ok("success");
    }

    @PutMapping
    public Result<String> updateTodo(@Validated @RequestBody Todo todo) {
        todoService.updateTodo(todo);
        return Result.ok("id为" + todo.getId() + "的数据更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteTodo(@Validated @PathVariable("id") @NotNull @Min(1) Long id) {
        todoService.deleteTodo(id);
        return Result.ok("id为" + id + "的数据删除成功");
    }

    @DeleteMapping
    public Result<String> deleteTodos(@Validated @RequestParam("ids") @NotEmpty List<Long> ids) {
        int num = todoService.deleteTodos(ids);
        return Result.ok("删除了" + num + "条数据");
    }
}