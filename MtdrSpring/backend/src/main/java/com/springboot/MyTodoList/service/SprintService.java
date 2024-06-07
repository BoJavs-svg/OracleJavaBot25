package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.Sprint;
import com.springboot.MyTodoList.model.Task;
import com.springboot.MyTodoList.model.Team;
import com.springboot.MyTodoList.model.TelegramUser;
import com.springboot.MyTodoList.repository.SprintRepository;

import oracle.security.crypto.cert.SPKAC;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SprintService {

    @Autowired
    private SprintRepository sprintRepository;
    public SprintService(SprintRepository sprintRepository){
        this.sprintRepository=sprintRepository;
    }

    public List<Sprint> findAll() {
        return sprintRepository.findAll();
    }

    public Optional<Sprint> getSprintById(Long id) {
        return sprintRepository.findById(id);
    }
    public List<Sprint> getTeamSprints(Long teamId){
        return sprintRepository.findByTeamId(teamId);
    }


    public Sprint addSprint(Sprint sprint) {
        return sprintRepository.save(sprint);
    }

    public boolean deleteSprint(Long id) {
        try {
            sprintRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Sprint updateSprint(Long id, Sprint updatedSprint) {
        Optional<Sprint> existingSprint = sprintRepository.findById(id);
        if (existingSprint.isPresent()) {
            Sprint sprintToUpdate = existingSprint.get();
            // Assuming Sprint has setters for all fields
            sprintToUpdate.setTitle(updatedSprint.getTitle());
            sprintToUpdate.setStatus(updatedSprint.getStatus());
            sprintToUpdate.setStartDate(updatedSprint.getStartDate());
            sprintToUpdate.setEndDate(updatedSprint.getEndDate());
            sprintToUpdate.setTeamID(updatedSprint.getTeamID());
            // Add other fields as necessary
            return sprintRepository.save(sprintToUpdate);
        } else {
            return null;
        }
    }
    
    public List<Sprint> getCurrentSprint(Long teamId){
        return sprintRepository.getCurrentSprint(teamId);
    }
}
