package com.springboot.MyTodoList.util;

import javax.ws.rs.DELETE;

public enum BotLabels {
	
	SHOW_MAIN_SCREEN("Show Main Screen"), 
	HIDE_MAIN_SCREEN("Hide Main Screen"),
	LIST_ALL_ITEMS("List All Items"), 
	ADD_NEW_TASK("Add New Task"),
	CHECK_MY_TASKS("Check My Tasks"),
	UPDATE_TASK("Update Task"),
	CREATE_TEAM("Create Team"),
	DONE("DONE"),
	UNDO("UNDO"),
	DELETE("DELETE"),
	MY_TODO_LIST("MY TODO LIST"),
	DASH("-"),
	VIEW_TEAM_TASKS("View Team Tasks"),
	EDIT_TEAM("Edit Team"),
	VIEW_TEAM_MEMBERS("View Team Members"),
	DELETE_TEAM("Delete Team");

	private String label;

	BotLabels(String enumLabel) {
		this.label = enumLabel;
	}

	public String getLabel() {
		return label;
	}

}
