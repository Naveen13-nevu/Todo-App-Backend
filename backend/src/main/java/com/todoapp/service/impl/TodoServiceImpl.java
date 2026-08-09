package com.todoapp.service.impl;

import com.todoapp.dto.TodoRequest;
import com.todoapp.dto.TodoResponse;
import com.todoapp.dto.TodoStatsResponse;
import com.todoapp.entity.Priority;
import com.todoapp.entity.Todo;
import com.todoapp.entity.TodoStatus;
import com.todoapp.entity.User;
import com.todoapp.exception.ResourceNotFoundException;
import com.todoapp.repository.TodoRepository;
import com.todoapp.repository.UserRepository;
import com.todoapp.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TodoServiceImpl implements TodoService {

    private final TodoRepository todoRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public TodoResponse create(Long userId, TodoRequest request) {
        User user = getUser(userId);

        Todo todo = Todo.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .priority(request.getPriority() != null ? request.getPriority() : Priority.MEDIUM)
                .status(request.getStatus() != null ? request.getStatus() : TodoStatus.PENDING)
                .user(user)
                .build();

        return toResponse(todoRepository.save(todo));
    }

    @Override
    @Transactional
    public TodoResponse update(Long userId, Long todoId, TodoRequest request) {
        Todo todo = getOwnedTodo(userId, todoId);

        todo.setTitle(request.getTitle());
        todo.setDescription(request.getDescription());
        todo.setDueDate(request.getDueDate());
        if (request.getPriority() != null) {
            todo.setPriority(request.getPriority());
        }
        if (request.getStatus() != null) {
            todo.setStatus(request.getStatus());
        }

        return toResponse(todoRepository.save(todo));
    }

    @Override
    @Transactional
    public void delete(Long userId, Long todoId) {
        Todo todo = getOwnedTodo(userId, todoId);
        todoRepository.delete(todo);
    }

    @Override
    public TodoResponse getById(Long userId, Long todoId) {
        return toResponse(getOwnedTodo(userId, todoId));
    }

    @Override
    public List<TodoResponse> getAll(Long userId, TodoStatus status, String search, String sortBy, String direction) {
        List<Todo> todos = todoRepository.search(userId, status, (search == null || search.isBlank()) ? null : search);

        Comparator<Todo> comparator = switch (sortBy == null ? "" : sortBy) {
            case "dueDate" -> Comparator.comparing(Todo::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()));
            case "priority" -> Comparator.comparing(t -> priorityRank(t.getPriority()));
            default -> Comparator.comparing(Todo::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()));
        };

        if ("desc".equalsIgnoreCase(direction)) {
            comparator = comparator.reversed();
        }

        return todos.stream()
                .sorted(comparator)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TodoStatsResponse getStats(Long userId) {
        long total = todoRepository.countByUserId(userId);
        long completed = todoRepository.countByUserIdAndStatus(userId, TodoStatus.COMPLETED);
        long pending = todoRepository.countByUserIdAndStatus(userId, TodoStatus.PENDING);

        return TodoStatsResponse.builder()
                .total(total)
                .completed(completed)
                .pending(pending)
                .build();
    }

    @Override
    @Transactional
    public TodoResponse updateStatus(Long userId, Long todoId, TodoStatus status) {
        Todo todo = getOwnedTodo(userId, todoId);
        todo.setStatus(status);
        return toResponse(todoRepository.save(todo));
    }

    private int priorityRank(Priority priority) {
        return switch (priority) {
            case HIGH -> 0;
            case MEDIUM -> 1;
            case LOW -> 2;
        };
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Todo getOwnedTodo(Long userId, Long todoId) {
        return todoRepository.findByIdAndUserId(todoId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found with id: " + todoId));
    }

    private TodoResponse toResponse(Todo todo) {
        return TodoResponse.builder()
                .id(todo.getId())
                .title(todo.getTitle())
                .description(todo.getDescription())
                .dueDate(todo.getDueDate())
                .priority(todo.getPriority())
                .status(todo.getStatus())
                .createdAt(todo.getCreatedAt())
                .updatedAt(todo.getUpdatedAt())
                .build();
    }
}
