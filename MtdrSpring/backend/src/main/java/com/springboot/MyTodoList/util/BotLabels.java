package com.springboot.MyTodoList.util;

public enum BotLabels {
	
	SHOW_MAIN_SCREEN("Show Main Screen"), 
	HIDE_MAIN_SCREEN("Hide Main Screen"),
		
	// User Commands
	EDIT_USER("Edit User"),                     // Label for /edituser command
	DELETE_USER("Delete User"),                 // Label for /deleteuser command
 
	// Sprint Commands
	CREATE_SPRINT("Create Sprint"),             // Label for /createsprint command
	VIEW_ALL_SPRINT_TASKS("View All Sprint Tasks"), // Label for /viewsprinttasks command
	EDIT_SPRINT("Edit Sprint"),                 // Label for /editsprint command
	DELETE_SPRINT("Delete Sprint"),             // Label for /deletesprint command
	 
	// Team Commands
	CREATE_TEAM("Create Team"),                 // Label for /createteam command
	VIEW_TEAM_TASKS("View Team Tasks"),         // Label for /viewteamtasks command
	EDIT_TEAM("Edit Team"),                     // Label for /editteam command
	DELETE_TEAM("Delete Team"),                 // Label for /deleteteam command
 	// Task Commands
	ADD_NEW_TASK("Add New Task"),
	M_ADD_TASK("Manager Add Task"),             // Label for /manageraddtask command
	CHECK_MY_TASKS("Check My Tasks"),
	EDIT_TASK("Edit Task"),                     // Label for /edittask command
	FINISH_TASK("Finish Task"),                 // Label for /finishtask command
	VIEW_TASK("View Task"),                     // Label for /viewtask command
	DELETE_TASK("Delete Task");                 // Label for /deletetask command
	
	private String label;

	BotLabels(String enumLabel) {
		this.label = enumLabel;
	}

	public String getLabel() {
		return label;
	}

}
