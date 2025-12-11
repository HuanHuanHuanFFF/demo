package com.hf.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hf.demo.domain.dto.PageDTO;
import com.hf.demo.domain.dto.Todo;
import com.hf.demo.domain.enums.SortDir;
import com.hf.demo.domain.query.TodoPageQuery;
import com.hf.demo.domain.vo.CodeStatus;
import com.hf.demo.exception.BizException;
import com.hf.demo.mapper.TodoMapper;
import com.hf.demo.service.TodoCacheAsyncService;
import com.hf.demo.service.TodoService;
import com.hf.demo.util.RandomUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class TodoServiceImpl implements TodoService {
    @Resource
    private TodoMapper todoMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private TodoCacheAsyncService todoCacheAsyncService;

    private static final String TODO_CACHE_KEY_PREFIX = "todo:byId:";
    private static final String NULL_VALUE = "NULL";
    private static final long TODO_CACHE_TTL_SECONDS = 30 * 60;
    private static final long TODO_CACHE_JITTER_SECONDS = 5 * 60;
    private static final long NULL_CACHE_TTL_SECONDS = 60;
    private static final String TODO_LIST_CACHE_KEY_PREFIX = "todo:list:";
    private static final long TODO_LIST_CACHE_TTL_SECONDS = 3 * 60;
    private static final long TODO_LIST_CACHE_JITTER_SECONDS = 2 * 60;
    private static final String TODO_LIST_VER_KEY = "todo:list:ver";

    @Override
    public List<Todo> listTodos() {
        return todoMapper.selectList(null);
    }

    @Override
    public PageDTO<Todo> pageTodos(TodoPageQuery query) {
        String title = query.getTitle() == null ? "" : query.getTitle();
        String ver = getListVersion();
        String k = TODO_LIST_CACHE_KEY_PREFIX + "v" + ver + ":" + query.getPageIndex() + "-" + query.getPageSize() + "-" + query.getSortBy() + "-" + query.getSortDir() + "-" + title;
        String cache = stringRedisTemplate.opsForValue().get(k);
        if (cache != null) {
            log.info("CACHE_HIT: todo:list v{} page={} size={} sortBy={} sortDir={} title={}", ver, query.getPageIndex(), query.getPageSize(), query.getSortBy(), query.getSortDir(), title);
            try {
                return objectMapper.readValue(cache, new TypeReference<PageDTO<Todo>>() {
                });
            } catch (JsonProcessingException e) {
                log.warn("pageTodos:{}查询命中,但序反列化失败", query);
            }
        }

        log.info("CACHE_MISS: todo:list v{} page={} size={} sortBy={} sortDir={} title={}", ver, query.getPageIndex(), query.getPageSize(), query.getSortBy(), query.getSortDir(), title);

        Page<Todo> page = new Page<>(query.getPageIndex(), query.getPageSize());
        QueryWrapper<Todo> wrapper = new QueryWrapper<>();
        String column = query.getSortBy().getColumn();
        if (query.getSortDir() == SortDir.ASC) {
            wrapper.orderByAsc(column);
        } else {
            wrapper.orderByDesc(column);
        }
        if (query.getTitle() != null && !query.getTitle().isEmpty()) wrapper.like("title", query.getTitle());
        Page<Todo> result = todoMapper.selectPage(page, wrapper);
        PageDTO<Todo> pageDTO = PageDTO.create(result);
        try {
            String v = objectMapper.writeValueAsString(pageDTO);
            stringRedisTemplate.opsForValue().set(k, v, Duration.ofSeconds(TODO_LIST_CACHE_TTL_SECONDS + RandomUtils.randomJitterSeconds(TODO_LIST_CACHE_JITTER_SECONDS)));
        } catch (JsonProcessingException e) {
            log.warn("pageTodos查询结果添加缓存失败", e);
        }
        return pageDTO;
    }

    @Override
    public Todo getById(Long id) {
        String k = TODO_CACHE_KEY_PREFIX + id;
        String cache = stringRedisTemplate.opsForValue().get(k);
        if (cache != null) {
            log.info("CACHE_HIT: todo:{}", id);
            if (NULL_VALUE.equals(cache)) return null;
            try {
                return objectMapper.readValue(cache, Todo.class);
            } catch (JsonProcessingException e) {
                log.warn("id:{}命中,但序反列化失败", id);
            }
        }

        log.info("CACHE_MISS: todo:{}", id);

        Todo todo = todoMapper.selectById(id);
        if (todo == null) {
            stringRedisTemplate.opsForValue().set(k, NULL_VALUE, Duration.ofSeconds(NULL_CACHE_TTL_SECONDS));
            return null;
        }
        try {
            String v = objectMapper.writeValueAsString(todo);
            stringRedisTemplate.opsForValue().set(k, v, Duration.ofSeconds(TODO_CACHE_TTL_SECONDS + RandomUtils.randomJitterSeconds(TODO_CACHE_JITTER_SECONDS)));
        } catch (JsonProcessingException e) {
            log.warn("getById中Redis缓存设置失败");
        }
        return todo;
    }

    @Override
    public void addTodo(Todo todo) {
        todo.setId(null);
        todo.setCreatedTime(LocalDateTime.now());
        todoMapper.insert(todo);
        deleteTodoCacheByIdWithDelay(todo.getId());
        stringRedisTemplate.opsForValue().increment(TODO_LIST_VER_KEY);
    }

    @Override
    public void updateTodo(Todo todo) {
        int rows = todoMapper.updateById(todo);
        if (rows == 0) throw new BizException(CodeStatus.UPDATE_CONFLICT, "数据已被其他请求修改或数据不存在");
        deleteTodoCacheByIdWithDelay(todo.getId());
        stringRedisTemplate.opsForValue().increment(TODO_LIST_VER_KEY);
    }

    @Override
    public void deleteTodo(Long id) {
        int rows = todoMapper.deleteById(id);
        if (rows == 0) throw new BizException(CodeStatus.NOT_FOUND);
        deleteTodoCacheByIdWithDelay(id);
        stringRedisTemplate.opsForValue().increment(TODO_LIST_VER_KEY);
    }

    @Override
    public int deleteTodos(List<Long> ids) {
        int rows = todoMapper.deleteByIds(ids);
        if (rows > 0) {
            ids.forEach(this::deleteTodoCacheByIdWithDelay);
            stringRedisTemplate.opsForValue().increment(TODO_LIST_VER_KEY);
        }
        return rows;
    }

    private void deleteTodoCacheByIdWithDelay(Long id) {
        String k = TODO_CACHE_KEY_PREFIX + id;
        stringRedisTemplate.delete(k);
        todoCacheAsyncService.deleteTodoCacheByIdWithDelay(id);
    }

    private String getListVersion() {
        String ver = stringRedisTemplate.opsForValue().get(TODO_LIST_VER_KEY);
        if (ver == null) {
            ver = "1";
            stringRedisTemplate.opsForValue().set(TODO_LIST_VER_KEY, ver);
        }
        return ver;
    }
}