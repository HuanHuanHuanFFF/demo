package com.hf.demo.service.impl;

import com.hf.demo.common.CodeStatus;
import com.hf.demo.dao.TodoDao;
import com.hf.demo.dto.Todo;
import com.hf.demo.exception.BizException;
import com.hf.demo.service.TodoService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TodoServiceImpl implements TodoService {
    @Resource
    private TodoDao todoDao;

    @Override
    public List<Todo> listTodos() {
        return todoDao.listTodos();
    }

    @Override
    public void addTodo(Todo todo) {
        todoDao.addTodo(todo);
    }

    @Override
    public void updateTodo(Todo todo) {
        int rows = todoDao.updateTodo(todo);
        if (rows == 0) throw new BizException(CodeStatus.NOT_FOUND);
    }

    @Override
    public void deleteTodo(Long id) {
        int rows = todoDao.deleteTodo(id);
        if (rows == 0) throw new BizException(CodeStatus.NOT_FOUND);
    }

    @Override
    public int deleteTodos(List<Long> ids) {
        return todoDao.deleteTodos(ids);
    }
}
