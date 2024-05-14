package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.TelegramUser;
import com.springboot.MyTodoList.repository.TelegramUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

import javax.transaction.Transactional;

@Service
@Transactional
public class TelegramUserService {

    @Autowired
    private TelegramUserRepository telegramUserRepository;    
    
    public String checkTelegramUserTableExists() {
        try {
            telegramUserRepository.getAllUsers();
            return "TelegramUser table exists and is accessible.";
        } catch (Exception e) {
            return "TelegramUser table does not exist or is not accessible: " + e.getMessage();
        }
    }
    //Save user
    @Autowired
    public TelegramUser saveTelegramUser(TelegramUser telegramUser) {
        try {
            return telegramUserRepository.save(telegramUser);
        } catch (Exception e) {
            // Log the exception or handle it as needed
            System.err.println("Error saving TelegramUser: " + e.getMessage());
            // Return null to indicate the save operation failed
            return null;
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