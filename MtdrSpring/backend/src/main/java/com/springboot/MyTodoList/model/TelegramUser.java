package com.springboot.MyTodoList.model;

import javax.persistence.*;


@Entity
@Table(name = "TELEGRAMUSER")
public class TelegramUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "ACCOUNT")
    private Long account;

    @Column(name = "ROL")
    private String rol;
    
     //Constructor
     public TelegramUser(){

     }
     public TelegramUser(String name, Long account, String rol){
        this.name = name;
        this.account = account;
        this.rol = rol;
    }

    //Getter and setters
     public int getId() {
        return id;
    }

    public void setId(int id) {
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
