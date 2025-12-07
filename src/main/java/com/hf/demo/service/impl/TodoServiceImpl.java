package com.hf.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hf.demo.dao.TodoDao;
import com.hf.demo.domain.dto.PageDTO;
import com.hf.demo.domain.dto.Todo;
import com.hf.demo.domain.enums.SortDir;
import com.hf.demo.domain.query.TodoPageQuery;
import com.hf.demo.domain.vo.CodeStatus;
import com.hf.demo.exception.BizException;
import com.hf.demo.mapper.TodoMapper;
import com.hf.demo.service.TodoService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TodoServiceImpl implements TodoService {
    @Resource
    private TodoDao todoDao;

    @Resource
    private TodoMapper todoMapper;

    @Override
    public List<Todo> listTodos() {
        return todoDao.listTodos();
    }

    @Override
    public PageDTO<Todo> pageTodos(TodoPageQuery query) {
        Page<Todo> page = new Page<>(query.getPageIndex(), query.getPageSize());
        QueryWrapper<Todo> wrapper = new QueryWrapper<>();
        String column = query.getSortBy().getColumn();
        if (query.getSortDir() == SortDir.ASC) {
            wrapper.orderByAsc(column);
        } else {
            wrapper.orderByDesc(column);
        }
        if (query.getTitle() != null && !query.getTitle().isEmpty())
            wrapper.like("title", query.getTitle());
        Page<Todo> result = todoMapper.selectPage(page, wrapper);
        return PageDTO.create(result);
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
