package com.hf.demo.service;

import com.hf.demo.dto.Todo;

import java.util.List;

public interface HelloService {
    List<Todo> listTodos();

    void addTodo(Todo todo);
}
