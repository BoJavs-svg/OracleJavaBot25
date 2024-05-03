package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.model.Team;
import com.springboot.MyTodoList.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    @Autowired
    private TeamService teamService;

    // @PostMapping
    // public Team createTeam(@RequestBody Team team) {
    //     return teamService.addTeam(team);
    // }
    @PostMapping
    public ResponseEntity<?> createTeam(@RequestBody Team team) {
    Team createdTeam = teamService.addTeam(team);
        if (createdTeam != null) {
            return new ResponseEntity<>(createdTeam, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>("Failed to create team", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public List<Team> getAllTeams() {
        return teamService.findAll();
    }

    // @GetMapping("/{teamId}")
    // public Optional<Team> getTeamById(@PathVariable Long teamId) {
    //     return teamService.getTeamById(teamId);
    // }
    @GetMapping("/{teamId}")
    public ResponseEntity<?> getTeamById(@PathVariable Long teamId) {
    Team team = teamService.getTeamById(teamId);
    if (team != null) {
        return new ResponseEntity<>(team, HttpStatus.OK);
    } else {
        return new ResponseEntity<>("Team not found", HttpStatus.NOT_FOUND);
    }
}

    @PutMapping("/{teamId}")
    public Team updateTeam(@PathVariable Long teamId, @RequestBody Team updatedTeam) {
        updatedTeam.setId(teamId); // Assuming Team has an ID field
        return teamService.updateTeam(teamId, updatedTeam);
    }

    @DeleteMapping("/{teamId}")
    public ResponseEntity<Void> deleteTeam(@PathVariable Long teamId) {
        boolean isDeleted = teamService.deleteTeam(teamId);
        if (isDeleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}