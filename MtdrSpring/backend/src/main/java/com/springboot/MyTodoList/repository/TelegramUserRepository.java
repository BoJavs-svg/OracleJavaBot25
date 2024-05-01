package com.springboot.MyTodoList.repository;


import com.springboot.MyTodoList.model.TelegramUser;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.Query;

@Repository
@Transactional
@EnableTransactionManagement
public interface TelegramUserRepository extends JpaRepository<TelegramUser, Long> {
    boolean existsByAccount(Long accountID);

    @Query(value = "SELECT * FROM TELEGRAMUSER", nativeQuery = true)
    void getAllUsers();

}
