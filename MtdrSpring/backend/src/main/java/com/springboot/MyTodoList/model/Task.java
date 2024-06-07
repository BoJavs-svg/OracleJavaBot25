package com.springboot.MyTodoList.model;

import javax.persistence.*;


@Entity
@Table(name = "TASK")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "description", length = 4000)
    private String description;

    @Column(name = "status", length = 255)
    private String status;

    @ManyToOne
    @JoinColumn(name = "userID", referencedColumnName = "id")
    private TelegramUser user;

    @OneToOne
    @JoinColumn(name = "sprintID", referencedColumnName = "id")
    private Sprint sprint;

    public Task() {
    }

    public Task(String description, String status, TelegramUser user, Sprint sprint) {
        this.description = description;
        this.status = status;
        this.user = user;
        this.sprint = sprint;
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public TelegramUser getUser() {
        return user;
    }

    public void setUser(TelegramUser user) {
        this.user = user;
    }

    public Sprint getSprint() {
        return sprint;
    }

    public void setSprint(Sprint sprint) {
        this.sprint = sprint;
    }

    @Override
    public String toString() {
        return "- "+id + " " + description + " " + status;
    }


}
