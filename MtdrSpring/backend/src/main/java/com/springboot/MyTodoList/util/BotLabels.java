package com.springboot.MyTodoList.util;

public enum BotLabels {
	
	SHOW_MAIN_SCREEN("Show Main Screen"), 
	HIDE_MAIN_SCREEN("Hide Main Screen"),
	LIST_ALL_ITEMS("List All Items"), 
	ADD_NEW_TASK("Add New Task"),
	CHECK_MY_TASKS("Check My Tasks"),
	CREATE_SPRINT("Create New Sprint"),
	VIEW_ALL_SPRINT_TASKS("View All Sprint Tasks"),
	DELETE_SPRINT("Delete Sprint"),


	DONE("DONE"),
	UNDO("UNDO"),
	DELETE("DELETE"),
	MY_TODO_LIST("MY TODO LIST"),
	DASH("-");

	private String label;

	BotLabels(String enumLabel) {
		this.label = enumLabel;
	}

	public String getLabel() {
		return label;
	}

}
