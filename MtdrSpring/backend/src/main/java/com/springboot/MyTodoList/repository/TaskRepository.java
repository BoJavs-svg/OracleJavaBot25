package com.springboot.MyTodoList.repository;


import com.springboot.MyTodoList.model.Sprint;
import com.springboot.MyTodoList.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.List;

import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository

public interface TaskRepository extends JpaRepository<Task, Long> {
        @Transactional
        void deleteBySprintId(Long sprintId);

        @Query(value = "SELECT * FROM TASK WHERE userID = :userId", nativeQuery = true)
        List<Task> findByUserId(@Param("userId") Long userId);

    }
