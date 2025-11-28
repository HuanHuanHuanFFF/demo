package com.hf.demo.service.impl;

import com.hf.demo.dao.HelloDao;
import com.hf.demo.dto.Todo;
import com.hf.demo.service.HelloService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HelloServiceImpl implements HelloService {
    @Resource
    private HelloDao helloDao;

    @Override
    public List<Todo> listTodos() {
        return helloDao.listTodos();
    }

    @Override
    public void addTodo(Todo todo) {
        helloDao.addTodo(todo);
    }
}
