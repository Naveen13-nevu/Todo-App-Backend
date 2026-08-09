package com.todoapp.repository;

import com.todoapp.entity.Todo;
import com.todoapp.entity.TodoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    List<Todo> findByUserId(Long userId);

    Optional<Todo> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, TodoStatus status);

    @Query("SELECT t FROM Todo t WHERE t.user.id = :userId " +
            "AND (:status IS NULL OR t.status = :status) " +
            "AND (CAST(:search AS string) IS NULL " +
            "     OR LOWER(t.title) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
            "     OR LOWER(t.description) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))")
    List<Todo> search(@Param("userId") Long userId,
                      @Param("status") TodoStatus status,
                      @Param("search") String search);
}
