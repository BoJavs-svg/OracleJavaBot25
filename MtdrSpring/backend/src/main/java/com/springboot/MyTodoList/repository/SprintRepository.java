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

    @Query(value="SELECT * FROM Sprint WHERE TEAMID = Team_ID;", nativeQuery=true)
    List<Sprint> getTeamSprints(Long Team_ID); 
    
}


// public interface SprintRepository extends JpaRepository<Sprint, Long> {
//     // SELECT * FROM Sprint WHERE NOW() BETWEEN startDate AND endDate
//     // t.description, t.status 
    
//         // Para Manager, devuelve todos los tasks de sprint actual
//         @Query(value = "SELECT t.DESCRIPTION " + 
//                     "FROM TODOUSER.TASK t " + 
//                     "INNER JOIN TODOUSER.SPRINT s ON t.SPRINTID = s.ID " + 
//                     "WHERE s.teamID = teamID " +
//                     "AND CURRENT_TIMESTAMP BETWEEN s.startDate AND s.endDate", nativeQuery = true)
//         List<String> getMagCurrentSprintTasks(Long teamID); // Team teamID @Param("teamID") Long teamID
    
//         // Para dev, regresa sus tasks del sprint actual // SELECT t.description, t.status
//         @Query(value="SELECT t.DESCRIPTION " + 
//                     "FROM TODOUSER.TASK t " + 
//                     "INNER JOIN TODOUSER.SPRINT s ON t.SPRINTID = s.ID " + 
//                     "WHERE t.USERID = user " +
//                     "AND CURRENT_TIMESTAMP BETWEEN s.STARTDATE AND s.ENDDATE;", nativeQuery=true)
//         List<String> getDevCurrentSprintTasks(Long user); // TelegramUser @Param("user") Long user
        
//         @Query(value="SELECT * FROM TODOUSER.SPRINT s WHERE CURRENT_TIMESTAMP BETWEEN s.STARTDATE AND s.ENDDATE;", nativeQuery=true)
//         Sprint getCurrentSprint();
//     }
    