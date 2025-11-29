package com.hf.demo.dao;

import com.hf.demo.dto.Todo;
import jakarta.annotation.Resource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class TodoDao {
    @Resource
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public List<Todo> listTodos() {
        String sql = "SELECT * FROM todo";
        return namedParameterJdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Todo.class));
    }

    public void addTodo(Todo todo) {
        String sql = "INSERT INTO todo (title, created_time) VALUES (:title,now())";
        namedParameterJdbcTemplate.update(sql, new BeanPropertySqlParameterSource(todo));
    }

    public int updateTodo(Todo todo) {
        String sql = "UPDATE todo SET title = :title WHERE id = :id";
        return namedParameterJdbcTemplate.update(sql, new BeanPropertySqlParameterSource(todo));
    }

    public int deleteTodo(Long id) {
        String sql = "DELETE FROM todo WHERE id = :id";
        return namedParameterJdbcTemplate.update(sql, Map.of("id", id));
    }

    public int deleteTodos(List<Long> ids) {
        String sql = "DELETE FROM todo WHERE id IN (:ids)";
        return namedParameterJdbcTemplate.update(sql, Map.of("ids", ids));
    }
}
