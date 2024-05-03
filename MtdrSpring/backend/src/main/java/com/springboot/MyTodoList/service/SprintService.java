package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.Sprint;
import com.springboot.MyTodoList.repository.SprintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SprintService {

    @Autowired
    private SprintRepository SprintRepository;
    public List<Sprint> findAll(){
        List<Sprint> sprints = SprintRepository.findAll();
        return sprints;
    }
    public ResponseEntity<Sprint> getSprintById(int id){
        Optional<Sprint> sprintData = SprintRepository.findById(id);
        if (sprintData.isPresent()){
            return new ResponseEntity<>(sprintData.get(), HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    public Sprint addSprint(Sprint Sprint){
        return SprintRepository.save(Sprint);
    }

    public boolean deleteSprint(int id){
        try{
            SprintRepository.deleteById(id);
            return true;
        }catch(Exception e){
            return false;
        }
    }
    public Sprint updateSprint(int id, Sprint td){
        Optional<Sprint> toDoItemData = SprintRepository.findById(id);
        if(toDoItemData.isPresent()){
            Sprint Sprint = toDoItemData.get();
            Sprint.setId(id);
            Sprint.setTitle(td.getTitle());
            Sprint.setStatus(td.getStatus());
            Sprint.setStartDate(td.getStartDate());
            Sprint.setEndDate(td.getEndDate());
            Sprint.setTeamID(td.getTeamID());

            return SprintRepository.save(Sprint);
        }else{
            return null;
        }
    }

}