package com.hf.demo.dao;

import com.hf.demo.dto.Todo;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class HelloDao {
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public List<Todo> listTodos() {
        String sql = "SELECT * FROM todo";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Todo.class));
    }

    public void addTodo(Todo todo) {
        String sql = "INSERT INTO todo (title, created_time) VALUES (:title,now())";
        jdbcTemplate.update(sql, new BeanPropertySqlParameterSource(todo));
    }
}
