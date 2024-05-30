package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.Team;
import com.springboot.MyTodoList.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TeamService {

    @Autowired
    private TeamRepository teamRepository;

    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    public List<Team> findAll() {
        return teamRepository.findAll();
    }

    public Optional<Team> getTeamById(Long id) {
        return teamRepository.findById(id);
    }

    public Team addTeam(Team team) {
        try {
            return teamRepository.save(team);
        } catch (Exception e) {
            // Log the exception or handle it as needed
            System.err.println("Error saving Team: " + e.getMessage());
            // Optionally, rethrow the exception or handle it differently
            return null;
        }
    }

    public boolean deleteTeam(Long id) {
        try {
            teamRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Team updateTeam(Long id, Team updatedTeam) {
        Optional<Team> existingTeam = teamRepository.findById(id);
        if (existingTeam.isPresent()) {
            Team teamToUpdate = existingTeam.get();
            // Assuming Team has setters for all fields
            teamToUpdate.setName(updatedTeam.getName());
            teamToUpdate.setDescription(updatedTeam.getDescription());
            // Add other fields as necessary
            return teamRepository.save(teamToUpdate);
        } else {
            return null;
        }
    }
}
