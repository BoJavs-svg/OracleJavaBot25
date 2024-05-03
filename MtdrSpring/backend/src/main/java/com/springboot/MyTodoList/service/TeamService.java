    package com.springboot.MyTodoList.service;

    import com.springboot.MyTodoList.model.Team;
    import com.springboot.MyTodoList.repository.TeamRepository;
    import org.springframework.beans.factory.annotation.Autowired;
    // import org.springframework.http.HttpStatus;
    // import org.springframework.http.ResponseEntity;
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
    
        // public Optional<Team> getTeamById(Long id) {
        //     return teamRepository.findById(id);
        // }
        public Team getTeamById(Long id) {
            Optional<Team> optionalTeam = teamRepository.findById(id);
            return optionalTeam.orElse(null); // Devuelve el Team si está presente, de lo contrario, devuelve null
        }
    
        public Team addTeam(Team team) {
            return teamRepository.save(team);
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
                teamToUpdate.setManagerID(updatedTeam.getManagerID());
                // Add other fields as necessary
                return teamRepository.save(teamToUpdate);
            } else {
                return null;
            }
        }
    }