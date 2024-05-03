package com.springboot.MyTodoList.repository;


import com.springboot.MyTodoList.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.List;

import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.Query;

@Repository
@Transactional
public interface TeamRepository extends JpaRepository<Team, Long> {

}
