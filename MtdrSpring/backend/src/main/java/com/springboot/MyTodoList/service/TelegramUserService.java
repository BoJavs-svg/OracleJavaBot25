package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.TelegramUser;
import com.springboot.MyTodoList.repository.TelegramUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

@Service
public class TelegramUserService {

    @Autowired
    private TelegramUserRepository telegramUserRepository;    
    //Save user
    @Transactional
    public TelegramUser saveTelegramUser(TelegramUser telegramUser) {
        try {
            return telegramUserRepository.save(telegramUser);
        } catch (Exception e) {
            // Rethrow the exception with a more descriptive message
            throw new RuntimeException("Error saving TelegramUser: " + e.getMessage(), e);
        }
    }
    public boolean userExists(Long accountID){
        return telegramUserRepository.existsByAccount(accountID);
    }
    public Optional<TelegramUser> findById(Long id){
        return telegramUserRepository.findById(id);
    }
    public TelegramUser updateTelegramUser(Long id, TelegramUser updatedUser){
        Optional<TelegramUser> userOptional = telegramUserRepository.findById(id);
        if(userOptional.isPresent()){
            TelegramUser user = userOptional.get();
            user.setName(updatedUser.getName());
            user.setAccount(updatedUser.getAccount());
            user.setRol(updatedUser.getRol());
            return telegramUserRepository.save(user);
        }else{
            return null;
        }
    }
}