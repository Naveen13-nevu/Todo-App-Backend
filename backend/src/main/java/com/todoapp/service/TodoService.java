package com.todoapp.service;

import com.todoapp.dto.TodoRequest;
import com.todoapp.dto.TodoResponse;
import com.todoapp.dto.TodoStatsResponse;
import com.todoapp.entity.Priority;
import com.todoapp.entity.TodoStatus;

import java.util.List;

public interface TodoService {
    TodoResponse create(Long userId, TodoRequest request);
    TodoResponse update(Long userId, Long todoId, TodoRequest request);
    void delete(Long userId, Long todoId);
    TodoResponse getById(Long userId, Long todoId);
    List<TodoResponse> getAll(Long userId, TodoStatus status, String search, String sortBy, String direction);
    TodoStatsResponse getStats(Long userId);
    TodoResponse updateStatus(Long userId, Long todoId, TodoStatus status);
}
