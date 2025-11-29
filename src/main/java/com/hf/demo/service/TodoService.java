package com.hf.demo.service;

import com.hf.demo.dto.Todo;

import java.util.List;

public interface TodoService {
    List<Todo> listTodos();

    void addTodo(Todo todo);

    void updateTodo(Todo todo);

    void deleteTodo(Long id);

    int deleteTodos(List<Long> ids);
}
