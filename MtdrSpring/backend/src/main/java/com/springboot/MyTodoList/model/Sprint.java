package com.springboot.MyTodoList.model;

import javax.persistence.*;


@Entity
@Table(name = "SPRINT")
public class Sprint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long ID; 

    @Column(name = "TITLE")
    private String title;

    @Column(name = "STATUS")
    private String status;

   @Column(name = "START_DATE")
    private String startDate; 

    @Column(name = "END_DATE")
    private String endDate; 

    @ManyToOne
    @JoinColumn(name = "TEAMID", referencedColumnName = "id")
    private Team teamID;
    

    public Sprint() {   
    }

    public Sprint(String title, String status, String startDate, String endDate, Team teamID) {
        this.title = title;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.teamID = teamID;
    }

    public long getId() {
        return ID;
    }

    public void setId(long ID) {
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

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
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
        return "Sprint{" +
                "ID=" + ID +
                ", title='" + title + '\'' +
                ", status='" + status + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", teamID=" + teamID.toString() +
                '}';
    }
}

