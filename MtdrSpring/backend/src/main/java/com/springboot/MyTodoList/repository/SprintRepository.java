package com.springboot.MyTodoList.repository;

import com.springboot.MyTodoList.model.TelegramUser;
import com.springboot.MyTodoList.model.Sprint;
import com.springboot.MyTodoList.model.Task;
import com.springboot.MyTodoList.model.Team;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.List;

import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
@Transactional
public interface SprintRepository extends JpaRepository<Sprint, Long> {
   
    @Query(value = "SELECT * FROM SPRINT WHERE TEAMID = :team_Id", nativeQuery = true)
    List<Sprint> findByTeamId(@Param("team_Id") Long team_Id); 

 // @Param("teamID") Long teamID 
}


