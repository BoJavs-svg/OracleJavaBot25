package com.springboot.MyTodoList.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "Sprint")
public class Sprint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // @Column(name = "id")
    int ID;

    @Column(name = "title")
    private String title;

    @Column(name = "status")
    private String status;

    @Column(name = "startDate")
    private Date startDate;

    @Column(name = "endDate")
    private Date endDate;

    @Column(name = "teamID")
    private int teamID;

    public Sprint() {
    }

    public Sprint(String title, String status, Date startDate, Date endDate, int teamID) {
        this.title = title;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.teamID = teamID;
    }

    public int getId() {
        return ID;
    }

    public void setId(int ID) {
        this.ID = ID;
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

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public int getTeamID() {
        return teamID;
    }

    public void setTeamID(int teamID) {
        this.teamID = teamID;
    }

    @Override
    public String toString() {
        return "Sprint{" +
                "ID=" + ID +
                ", title='" + title + '\'' +
                ", status='" + status + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", teamID=" + teamID +
                '}';
    }
}