package com.springboot.MyTodoList.model;

import javax.persistence.*;



@Entity
@Table(name = "TELEGRAMUSER")
public class TelegramUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "ACCOUNT")
    private String account;

    @Column(name = "ROL")
    private String rol;
    
    @ManyToOne
    @JoinColumn(name = "TEAMID",  referencedColumnName = "id")
    private Team teamID;

     //Constructor
     public TelegramUser(){

     }
     public TelegramUser(String name, String account, String rol){
        this.name = name;
        this.account = account;
        this.rol = rol;
    }

    //Getter and setters
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

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public void setTeam(Team team) {
        this.teamID = team;
    }
    public Team getTeam() {
        return this.teamID;
    }

    @Override
    public String toString(){
        return "TelegramUser{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", account='" + account + '\'' +
        ", rol='" + rol + '\'' +
        '}';
    }
}