    package com.springboot.MyTodoList.controller;

    import com.springboot.MyTodoList.model.Sprint;
    import com.springboot.MyTodoList.service.SprintService;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;

    import java.util.List;
    import java.util.Optional;


    @RestController
    @RequestMapping("/api/sprints")
    public class SprintController {

        @Autowired
        private SprintService sprintService;

        @PostMapping
        public Sprint createSprint(@RequestBody Sprint sprint) {
            return sprintService.addSprint(sprint);
        }

        @GetMapping
        public List<Sprint> getAllSprints() {
            return sprintService.findAll();
        }

        @GetMapping
        public List<Sprint> getTeamSprints(@PathVariable Long teamId) {
            return sprintService.getTeamSprints(teamId);
        }

        @GetMapping("/{sprintId}")
        public Optional<Sprint> getSprintById(@PathVariable Long sprintId) {
            return sprintService.getSprintById(sprintId);
        }

        @PutMapping("/{sprintId}")
        public Sprint updateSprint(@PathVariable Long sprintId, @RequestBody Sprint updatedSprint) {
            updatedSprint.setId(sprintId); // Assuming Sprint has an ID field
            return sprintService.updateSprint(sprintId, updatedSprint);
        }

        @DeleteMapping("/{sprintId}")
        public ResponseEntity<Void> deleteSprint(@PathVariable Long sprintId) {
            boolean isDeleted = sprintService.deleteSprint(sprintId);
            if (isDeleted) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }
