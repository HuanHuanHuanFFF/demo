package com.hf.demo.service;

import com.hf.demo.domain.dto.PageDTO;
import com.hf.demo.domain.dto.Todo;
import com.hf.demo.domain.query.TodoPageQuery;

import java.util.List;

public interface TodoService {
    List<Todo> listTodos();

    PageDTO<Todo> pageTodos(TodoPageQuery query);

    void addTodo(Todo todo);

    void updateTodo(Todo todo);

    void deleteTodo(Long id);

    int deleteTodos(List<Long> ids);
}
