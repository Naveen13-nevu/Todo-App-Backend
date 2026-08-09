package com.todoapp.controller;

import com.todoapp.dto.TodoRequest;
import com.todoapp.dto.TodoResponse;
import com.todoapp.dto.TodoStatsResponse;
import com.todoapp.entity.TodoStatus;
import com.todoapp.security.UserPrincipal;
import com.todoapp.service.TodoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    @PostMapping
    public ResponseEntity<TodoResponse> create(@AuthenticationPrincipal UserPrincipal principal,
                                                @Valid @RequestBody TodoRequest request) {
        TodoResponse response = todoService.create(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TodoResponse>> getAll(@AuthenticationPrincipal UserPrincipal principal,
                                                       @RequestParam(required = false) TodoStatus status,
                                                       @RequestParam(required = false) String search,
                                                       @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
                                                       @RequestParam(required = false, defaultValue = "desc") String direction) {
        List<TodoResponse> todos = todoService.getAll(principal.getId(), status, search, sortBy, direction);
        return ResponseEntity.ok(todos);
    }

    @GetMapping("/stats")
    public ResponseEntity<TodoStatsResponse> getStats(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(todoService.getStats(principal.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TodoResponse> getById(@AuthenticationPrincipal UserPrincipal principal,
                                                 @PathVariable Long id) {
        return ResponseEntity.ok(todoService.getById(principal.getId(), id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TodoResponse> update(@AuthenticationPrincipal UserPrincipal principal,
                                                @PathVariable Long id,
                                                @Valid @RequestBody TodoRequest request) {
        return ResponseEntity.ok(todoService.update(principal.getId(), id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TodoResponse> updateStatus(@AuthenticationPrincipal UserPrincipal principal,
                                                       @PathVariable Long id,
                                                       @RequestParam TodoStatus status) {
        return ResponseEntity.ok(todoService.updateStatus(principal.getId(), id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal,
                                        @PathVariable Long id) {
        todoService.delete(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
