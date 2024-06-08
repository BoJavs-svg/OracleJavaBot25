package com.springboot.MyTodoList.repository;


import com.springboot.MyTodoList.model.TelegramUser;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.List;

import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


@Repository
@Transactional
public interface TelegramUserRepository extends JpaRepository<TelegramUser, Long> {
    boolean existsByAccount(String account);

    @Query(value = "SELECT * FROM TELEGRAMUSER", nativeQuery = true)
    List<TelegramUser> getAllUsers();

    @Query(value = "SELECT * FROM TELEGRAMUSER WHERE account = ?1", nativeQuery = true)
    List<TelegramUser> findByAccount(String account);

    @Query(value = "SELECT * FROM TELEGRAMUSER WHERE teamID = ?1", nativeQuery = true)
    List<TelegramUser> findByTeamId(Long teamId);

 
    @Query(value = "SELECT * FROM TELEGRAMUSER WHERE TEAMID = :team_Id", nativeQuery = true)
    List<TelegramUser> findByTeam(@Param("team_Id") Long team_Id);
    
}
