package com.springboot.MyTodoList.repository;


import com.springboot.MyTodoList.model.Task;
import com.springboot.MyTodoList.model.Sprint;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.List;

import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.Query;

@Repository
@Transactional
public interface TaskRepository extends JpaRepository<Task, Long> {
        List<Task> findByUserId(long userId);

        @Query(value="SELECT * FROM TODOUSESR.TASK WHERE SPRINTID = sprint_Id;", nativeQuery=true)
        List<Task> findBySprintId(long sprint_Id);

}
