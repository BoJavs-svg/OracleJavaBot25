package com.springboot.MyTodoList.model;

import javax.persistence.*;

import com.springboot.MyTodoList.repository.TeamRepository;

import java.io.Serializable;
// import java.util.Date;
import java.util.Optional;

@Entity
@Table(name = "TEAM")
public class Team implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAME", nullable = false, length = 255)
    private String name;

    @Column(name = "DESCRIPTION", nullable = false, length = 4000)
    private String description;

    @ManyToOne
    @JoinColumn(name = "MANAGERID", nullable = false,  referencedColumnName = "id")
    private TelegramUser managerID;

    // Constructor vacío
    public Team() {}

    // Constructor con todos los atributos
    public Team(String name, String description, TelegramUser managerID) {
        this.name = name;
        this.description = description;
        this.managerID = managerID;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TelegramUser getManagerID() {
        return managerID;
    }

    public void setManagerID(TelegramUser managerID) {
        this.managerID = managerID;
    }

    // Sobrescribir toString para facilitar la depuración
    @Override
    public String toString() {
        return "Team{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", managerID=" + managerID.toString() +
                '}';
    }
}