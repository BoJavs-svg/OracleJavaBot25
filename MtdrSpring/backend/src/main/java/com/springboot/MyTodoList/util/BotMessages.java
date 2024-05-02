package com.springboot.MyTodoList.util;

public enum BotMessages {
	
	HELLO_MYTODO_BOT(
	"Holaaaaa!1! OwO. Soy J-Jaime yy hoy seré tu bot >////< *Se sonroja, se viene, se resbala con sus fliudos y se rompe el cuello*"), // "Hello! I'm MyTodoList Bot!\nType a new todo item below and press the send button (blue arrow), or select an option below:"
	BOT_REGISTERED_STARTED("Bot registered and started succesfully!"),
	ITEM_DONE("Item done! Select /todolist to return to the list of todo items, or /start to go to the main screen."), 
	ITEM_UNDONE("Item undone! Select /todolist to return to the list of todo items, or /start to go to the main screen."), 
	ITEM_DELETED("Item deleted! Select /todolist to return to the list of todo items, or /start to go to the main screen."),
	TYPE_NEW_TODO_ITEM("Type a new todo item below and press the send button (blue arrow) on the rigth-hand side."),
	NEW_ITEM_ADDED("New item added! Select /todolist to return to the list of todo items, or /start to go to the main screen."),
	BYE("Bye! Select /start to resume!"),
	NEW_SPRINT_CREATED("Create a new Sprint\nPlease enter the information in te next format:\n[title] [status] [startDate dd-MM-yyyy] [endDate dd-MM-yyyy] [teamID]\nexample:\n[Jaimear] [In progress] [11-11-2022] [22-12-2202] [2]"),
	NEW_SPRINT_ADDED("Sprint added!"),
	SPRINT_SYN_ERROR("Não não, Mala sintaxis. Por favor, reescreva o sprint corretamente ou sofra as consequências."),
	EERROORR("Hubo un error. PERDOOOOOOOOON"); // Feelin' cute. Mey delete this text later

	private String message;

	BotMessages(String enumMessage) {
		this.message = enumMessage;
	}

	public String getMessage() {
		return message;
	}

}
