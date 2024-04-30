package com.springboot.MyTodoList.model;

import javax.persistence.*;

@Entity
@Table(name = "telegramUser")
public class TelegramUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
     Long id;
    @Column(name = "name")
     String name;
     @Column(name = "account")
     Long account;
     @Column(name = "rol")
     String rol;
    
     //Constructor
     public TelegramUser(){

     }
     public TelegramUser(Long id, String name, Long account, String rol){
        this.id = id;
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

    public Long getAccount() {
        return account;
    }

    public void setAccount(Long account) {
        this.account = account;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
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
