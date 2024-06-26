package com.springboot.MyTodoList.repository;


import com.springboot.MyTodoList.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.List;

import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.Query;

@Repository
@Transactional
public interface TaskRepository extends JpaRepository<Task, Long> {

        @Query(value = "SELECT * FROM TASK WHERE userid = ?1", nativeQuery = true)
        List<Task> findByUserId(long userId);
}
