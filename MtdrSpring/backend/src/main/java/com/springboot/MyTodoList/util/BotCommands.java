package com.springboot.MyTodoList.util;

public enum BotCommands {
	TODO_LIST("/todolist"),
	CHECK_TASKS("/mytasks"),
	ADD_TASK("/addtask"),

	// User Commands
    	START_COMMAND("/start"),
    	HIDE_COMMAND("/hide"),
    	CREATE_USER("/createuser"),
    	EDIT_USER("/edituser"),
    	DELETE_USER("/deleteuser"),

    	// Sprint Commands
    	CREATE_SPRINT("/createsprint"),
    	VIEW_ALL_SPRINT_TASKS("/viewsprinttasks"),
    	EDIT_SPRINT("/editsprint"),
    	DELETE_SPRINT("/deletesprint"),

    	// Team Commands
    	CREATE_TEAM("/createteam"),
    	VIEW_TEAM_TASKS("/viewteamtasks"),
    	EDIT_TEAM("/editteam"),
    	DELETE_TEAM("/deleteteam"),
	
    	// Task Commands
    	CREATE_TASK("/createtask"),
    	EDIT_TASK("/edittask"),
    	FINISH_TASK("/finishtask"),
    	VIEW_USER_TASKS("/viewusertasks"),
		VIEW_DEVELOPER_TASKS("/mytasks"),
		VIEW_TASK("/viewtask"),
		DELETE_TASK("/deletetask");

	private String command;

	BotCommands(String enumCommand) {
		this.command = enumCommand;
	}

	public String getCommand() {
		return command;
	}
}
