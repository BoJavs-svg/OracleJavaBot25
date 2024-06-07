package com.springboot.MyTodoList.util;

public enum BotCommands {
	  // User Commands
	  START_COMMAND("/start"),   //      // Create user
	  HIDE_COMMAND("/hide"),
	  EDIT_USER("/edituser"),          // Edit user
	  DELETE_USER("/deleteuser"),      // Delete user
  
	  // Sprint Commands
	  CREATE_SPRINT("/createsprint"),   // Create sprint
	  VIEW_ALL_SPRINT_TASKS("/viewsprinttasks"), // View all sprint tasks
	  EDIT_SPRINT("/editsprint"),       // Edit sprint
	  DELETE_SPRINT("/deletesprint"),   // Delete sprint
  
	  // Team Commands
	  CREATE_TEAM("/createteam"),       // Create team
	  VIEW_TEAM_TASKS("/viewteamtasks"), // View team tasks
	  EDIT_TEAM("/editteam"),           // Edit team
	  DELETE_TEAM("/deleteteam"),       // Delete team
  
	  // Task Commands
	  ADD_TASK("/addtask"),         //    // Create task
	  M_ADD_TASK("/manageraddtask"),    // Manager add task
	  CHECK_TASKS("/mytasks"),     //     // Developer view all tasks
	  EDIT_TASK("/edittask"),           // Edit task
	  FINISH_TASK("/finishtask"),       // Finish task
	  VIEW_TASK("/viewtask"),           // View specific task
	  DELETE_TASK("/deletetask");       // Delete task

	private String command;

	BotCommands(String enumCommand) {
		this.command = enumCommand;
	}

	public String getCommand() {
		return command;
	}
}
