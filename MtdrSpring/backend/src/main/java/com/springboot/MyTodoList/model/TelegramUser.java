package com.springboot.MyTodoList.model;

import javax.persistence.*;

@Entity
@Table(name = "TELEGRAMUSER")
public class TelegramUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
     Long ID;
    @Column(name = "NAME")
     String name;
     @Column(name = "ACCOUNT")
     Long account;
     @Column(name = "ROL")
     String rol;
    
     //Constructor
     public TelegramUser(){

     }
     public TelegramUser(Long id, String name, Long account, String rol){
        this.ID = id;
        this.name = name;
        this.account = account;
        this.rol = rol;
     }

    //Getter and setters
     public Long getId() {
        return ID;
    }

    public void setId(Long id) {
        this.ID = id;
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
        "id=" + ID +
        ", name='" + name + '\'' +
        ", account='" + account + '\'' +
        ", rol='" + rol + '\'' +
        '}';
    }
}
