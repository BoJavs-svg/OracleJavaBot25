package com.springboot.MyTodoList.model;

import javax.persistence.*;

@Entity
@Table(name = "SPRINT")
public class Sprint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id; 

    @Column(name = "TITLE")
    private String title;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "STARTDATE")
    private java.sql.Timestamp startDate; 

    @Column(name = "ENDDATE")
    private java.sql.Timestamp endDate; 

    @ManyToOne
    @JoinColumn(name = "TEAMID", referencedColumnName = "id")
    private Team teamID;
    

    public Sprint() {   
    }

    public Sprint(String title, String status, java.sql.Timestamp startDate, java.sql.Timestamp endDate, Team teamID) {
        this.title = title;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.teamID = teamID;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public java.sql.Timestamp getStartDate() {
        return startDate;
    }

    public void setStartDate(java.sql.Timestamp startDate) {
        this.startDate = startDate;
    }

    public java.sql.Timestamp getEndDate() {
        return endDate;
    }

    public void setEndDate(java.sql.Timestamp endDate) {
        this.endDate = endDate;
    }

    public Team getTeamID() {
        return teamID;
    }

    public void setTeamID(Team teamID) {
        this.teamID = teamID;
    }

    @Override
    public String toString() {
        return "- "+ id + "| " + title + "| " + status ;
    }
}
