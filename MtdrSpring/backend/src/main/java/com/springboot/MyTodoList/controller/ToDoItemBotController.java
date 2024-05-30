package com.springboot.MyTodoList.controller;

import static org.mockito.ArgumentMatchers.booleanThat;
import static org.mockito.ArgumentMatchers.eq;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

import org.hibernate.jdbc.Expectations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.springboot.MyTodoList.model.ToDoItem;
import com.springboot.MyTodoList.model.TelegramUser;
import com.springboot.MyTodoList.model.Sprint;
import com.springboot.MyTodoList.model.Task;
import com.springboot.MyTodoList.model.Team;

import com.springboot.MyTodoList.service.ToDoItemService;
import com.springboot.MyTodoList.service.TelegramUserService;
import com.springboot.MyTodoList.service.TaskService;
import com.springboot.MyTodoList.service.SprintService;
import com.springboot.MyTodoList.service.TeamService;


import com.springboot.MyTodoList.util.BotCommands;
import com.springboot.MyTodoList.util.BotHelper;
import com.springboot.MyTodoList.util.BotLabels;
import com.springboot.MyTodoList.util.BotMessages;

import oracle.security.o3logon.a;

import java.util.Map;
import java.util.HashMap;


public class ToDoItemBotController extends TelegramLongPollingBot {

	private static final Logger logger = LoggerFactory.getLogger(ToDoItemBotController.class);
	@Autowired
	private ToDoItemService toDoItemService;
	@Autowired
	private TelegramUserService telegramUserService;
	@Autowired
	private TaskService taskService;
	@Autowired
	private SprintService sprintService;
	@Autowired
	private SprintService teamService;
	

	private String botName;
	private Map<Long, String> userStates = new HashMap<>();
	private Map<Long, TelegramUser> userMap = new HashMap<>();	
	private Map<Long,Task> tempTasks = new HashMap<>();
	
	public ToDoItemBotController(String botToken, String botName, ToDoItemService toDoItemService, TelegramUserService telegramUserService,TaskService taskService,SprintService sprintService,TeamService teamService) {
		super(botToken);
		logger.info("Bot Token: " + botToken);
		logger.info("Bot name: " + botName);
		this.botName = botName;
		this.toDoItemService = toDoItemService;
		this.telegramUserService=telegramUserService;
		this.taskService = taskService;
		this.sprintService=sprintService;
	}
	@Override
	public void onUpdateReceived(Update update) {
		if (update.hasMessage() && update.getMessage().hasText()&& update.getMessage().getFrom()!= null) {
			String messageTextFromTelegram = update.getMessage().getText();
			long chatId = update.getMessage().getChatId();
			String user_username = update.getMessage().getFrom().getUserName();
			if (!userStates.containsKey(chatId)) {
					userStates.put(chatId, null); // Initialize state for new user
				}
			logger.info("Received message ("+chatId+"): " + messageTextFromTelegram);
			SendMessage message = new SendMessage();
			if(user_username.isEmpty()|| user_username.isBlank()){
				try{
					message.setChatId(chatId);
					message.setText("You dont have a username, please finish setting up your telegram account, before using the BOT");
					execute(message);
				}catch(TelegramApiException e){
					logger.error(e.getLocalizedMessage());
				}
				return;
			}
			
			if (messageTextFromTelegram.equals(BotCommands.START_COMMAND.getCommand())
					|| messageTextFromTelegram.equals(BotLabels.SHOW_MAIN_SCREEN.getLabel())) {
				ResponseEntity<Boolean> response = findIfExists(user_username);
				logger.info("Response status code "+response.getBody());
				if (!response.getBody()) {
					message = new SendMessage();
					message.setChatId(chatId);
					message.setText("Ooops it seems you dont have a user. Please register");
					try{
						execute(message);
					}catch(TelegramApiException e){
						logger.error("Error en mensaje recibido");
					}
					promptForUserInformation(chatId);
				} else {
					try{

						SendMessage messageToTelegram = new SendMessage();
						messageToTelegram.setChatId(chatId);
						messageToTelegram.setText(BotMessages.HELLO_MYTODO_BOT.getMessage());
						execute(messageToTelegram);
						markupKB(user_username,chatId);
					}catch (Exception e){
						
					}
				}
		}else if (messageTextFromTelegram.equals(BotCommands.ADD_TASK.getCommand())
				|| messageTextFromTelegram.equals(BotLabels.ADD_NEW_TASK.getLabel())){
					Optional<TelegramUser> userOpt = telegramUserService.getUserbyAccount(user_username);
					if (userOpt.isPresent() && "Developer".equals(userOpt.get().getRol())) {
						try {
						SendMessage messageToTelegram = new SendMessage();
						messageToTelegram.setChatId(chatId);
						messageToTelegram.setText("Please enter the task description:");
							Task tempTask = new Task();
							tempTask.setUser(userOpt.get());
							tempTasks.put(chatId, tempTask);	
							execute(messageToTelegram);
							userStates.put(chatId, "WAITING_FOR_TASK_DESCRIPTION");

						} catch (Exception e) {
							logger.error(e.getLocalizedMessage(), e);
						}

					}else{
						SendMessage messageToTelegram = new SendMessage();
						messageToTelegram.setChatId(chatId);
						messageToTelegram.setText("Hi! This command is for Developers. You can add tasks to your team using /manageraddtask or using LABEL ");
						try {
							execute(messageToTelegram);
							userStates.put(chatId, null);
						} catch (TelegramApiException e) {
							logger.error(e.getLocalizedMessage(), e);
						}
					}
		}else if (messageTextFromTelegram.equals(BotCommands.CHECK_TASKS.getCommand())
			|| messageTextFromTelegram.equals(BotLabels.CHECK_MY_TASKS.getLabel())){
			try{
				Long id = telegramUserService.getUserbyAccount(user_username).get().getId();
				List<Task> tasks = taskService.getTasksByUserId(id);
				String t = "";
				for(Task task : tasks){
					t+=(task.toString()+"\n");
				}

				SendMessage messageToTelegram = new SendMessage();
				messageToTelegram.setChatId(chatId);
				messageToTelegram.setText(t); // Set the tasks string as the message text

				execute(messageToTelegram); // Send the message
			}catch (Exception e){
				logger.error("Error fetching tasks for user", e);

			}
		}else if (messageTextFromTelegram.equals(BotCommands.FINISH_TASK.getCommand())
		|| messageTextFromTelegram.equals(BotLabels.FINISH_TASK.getLabel())){
			try{
				Long id = telegramUserService.getUserbyAccount(user_username).get().getId();
				List<Task> tasks = taskService.getTasksByUserId(id);
				String t = "";
				// Convert tasks to keyboard buttons
				List<KeyboardRow> keyboardRows = new ArrayList<>();
				for (Task task : tasks) {
					KeyboardRow row = new KeyboardRow();
					row.add(task.getId().toString()); // Assuming Task has a getTitle() method that returns a readable title
					keyboardRows.add(row);
					t+=task.toString() + "\n";
				}
		
				SendMessage messageToTelegram = new SendMessage();
				messageToTelegram.setChatId(chatId);
				
				// Create a ReplyKeyboardMarkup object and set the keyboard
				ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
				keyboardMarkup.setKeyboard(keyboardRows);
				keyboardMarkup.setOneTimeKeyboard(true); // Optional: Makes the keyboard appear once per session
				keyboardMarkup.setResizeKeyboard(true); // Optional: Resizes the keyboard to fit the text entered
				keyboardMarkup.setSelective(true); // Optional: Allows the user to interact with the keyboard even if the focus is elsewhere
				
				// Attach the markup to the SendMessage object
				messageToTelegram.setReplyMarkup(keyboardMarkup);
				messageToTelegram.setText(t+"\nSelect a task to finish from the markup keyboard:");
				execute(messageToTelegram);
				userStates.put(chatId,"WAITING_FOR_COMPLETED_TASK");
			}catch (Exception e){
				logger.error(e.getLocalizedMessage(), e);
			}
		}else{
			//States
			if (userStates.get(chatId).equals("WAITING_FOR_NAME")) {
				TelegramUser telegramUser = new TelegramUser();
				telegramUser.setName(messageTextFromTelegram);
				telegramUser.setAccount(user_username);
				userMap.put(chatId,telegramUser);
				promptForRole(chatId);
		
			} else if (userStates.get(chatId).equals("WAITING_FOR_ROLE")) {
				try {
					TelegramUser temp= userMap.get(chatId);
					TelegramUser newTelegramUser = new TelegramUser(temp.getName(),user_username,messageTextFromTelegram);
					ResponseEntity entity = saveUser(newTelegramUser,chatId);		
				
					SendMessage messageToTelegram = new SendMessage();
					messageToTelegram.setChatId(chatId);
					messageToTelegram.setText("User created");
					userMap.put(chatId,null);
					userStates.put(chatId, null);
					messageToTelegram = new SendMessage();
					messageToTelegram.setChatId(chatId);
					messageToTelegram.setText(BotMessages.HELLO_MYTODO_BOT.getMessage());
					execute(messageToTelegram);				
					markupKB(user_username,chatId);
				} catch (Exception e) {
					logger.error(e.getLocalizedMessage(), e);
				}
			
			
			}else if(userStates.get(chatId).equals("WAITING_FOR_SPRINT_ASSIGN")) {
				Task tempTask = tempTasks.get(chatId);
				try {
					Long sprintId = Long.parseLong(messageTextFromTelegram);
					Optional<Sprint> sprint = sprintService.getSprintById(sprintId);
					if (!sprint.isPresent()) { 
						SendMessage messageToTelegram = new SendMessage();
						messageToTelegram.setChatId(chatId);
						messageToTelegram.setText("Invalid Sprint ID. Please try again.");
						execute(messageToTelegram);
					} else {
						Sprint assignedSprint = sprint.get(); 
						tempTask.setSprint(assignedSprint); 
						taskService.saveTask(tempTask); 	

						tempTasks.remove(chatId);
						SendMessage successMessage = new SendMessage();
						successMessage.setChatId(chatId);
						successMessage.setText("Task added successfully to sprint!");
						execute(successMessage);
					}
				} catch (NumberFormatException e) {
					logger.error("Invalid sprint ID format: {}. Error: {}", messageTextFromTelegram, e.getMessage());
				} catch (TelegramApiException e) {
					logger.error("Error executing command: {}. Error: {}", e.getMessage(), e);
				}
			}else if(userStates.get(chatId).equals("WAITING_FOR_TASK_DESCRIPTION")){
				try {
					Task tempTask = tempTasks.get(chatId);
					tempTask.setDescription(messageTextFromTelegram);
					tempTask.setStatus("NotStarted");
					SendMessage messageToTelegram = new SendMessage();
					List<Sprint> sprints = getAllSprints();
					if(sprints.isEmpty()){
						messageToTelegram.setChatId(chatId);
						messageToTelegram.setText("There are currently no active sprints. Please create a new sprint using /createsprint.");
						userStates.put(chatId,null);
					}else{
						StringBuilder sb = new StringBuilder("Available sprints:\n");
						List<KeyboardRow> keyboardRows = new ArrayList<>();
						for(Sprint sprint : sprints) {
							KeyboardRow row = new KeyboardRow();
							row.add(sprint.getId().toString()); 
							keyboardRows.add(row);
							sb.append("\n" + sprint.toString());
						}
						sb.append("\nPlease enter the sprintID:");
			
						messageToTelegram.setChatId(chatId);
						messageToTelegram.setText(sb.toString());
			
						// Create a ReplyKeyboardMarkup object and set the keyboard
						ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
						keyboardMarkup.setKeyboard(keyboardRows);
						keyboardMarkup.setOneTimeKeyboard(true);
						keyboardMarkup.setResizeKeyboard(true); 
						keyboardMarkup.setSelective(true); 
			
						// Attach the markup to the SendMessage object
						messageToTelegram.setReplyMarkup(keyboardMarkup);
						execute(messageToTelegram); 
					}
				userStates.put(chatId, "WAITING_FOR_SPRINT_ASSIGN");
			}catch (TelegramApiException e) {
				logger.error(e.getLocalizedMessage(), e);
			}
			}else if(userStates.get(chatId).equals("WAITING_FOR_COMPLETED_TASK")){
				try {
					Long taskId = Long.parseLong(messageTextFromTelegram);
					Optional<Task> task = taskService.findById(taskId);
					if(task.isPresent()){
						Task finished = taskService.markTaskAsFinished(task.get());
						SendMessage messageToTelegram = new SendMessage();
						messageToTelegram.setChatId(chatId);
						messageToTelegram.setText("Task was marked as finished");
						execute(messageToTelegram);
						userStates.put(chatId, null);
					}else{
						SendMessage messageToTelegram = new SendMessage();
						messageToTelegram.setChatId(chatId);
						messageToTelegram.setText("Wrong task id");
						execute(messageToTelegram);
			
					}
				}catch (Exception e){
					logger.error(e.getLocalizedMessage(), e);
				}

			}
		}
	}}
	
	@Override
	public String getBotUsername() {		
		return botName;
	}
	//TelegramUSER
	public ResponseEntity<Boolean> findIfExists(@PathVariable("chatId") String username){
		Boolean flag = false;
		try {
			flag = telegramUserService.userExists(username);
			return new ResponseEntity<>(flag, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			return new ResponseEntity<>(false, HttpStatus.NOT_FOUND);
		}

	}
	public ResponseEntity saveUser(@RequestBody TelegramUser telegramUser, long chatId) {
		try {
			if (telegramUser!= null) {
				TelegramUser savedUser = telegramUserService.saveTelegramUser(telegramUser);
				if (savedUser!= null) {
					HttpHeaders responseHeaders = new HttpHeaders();
					responseHeaders.set("location", "" + savedUser.getId());
					responseHeaders.set("Access-Control-Expose-Headers", "location");
					return ResponseEntity.ok().headers(responseHeaders).build();
				} else {
					// Handle the case where the user is null
					throw new IllegalArgumentException("No user saved");
				}
			} else {
				throw new IllegalArgumentException("telegramUser cannot be null");
			}
		} catch (Exception e) {
			// Log the exception or handle it as needed
			System.err.println("Error saving TelegramUser: " + e.getMessage());
			// Return a 500 Internal Server Error
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving TelegramUser");
		}
	}	
	public Optional<TelegramUser> getUserbyId(long id){
		return telegramUserService.getUser(id);
	}

	public Optional<Sprint> getSprintfromId(long id){
		return sprintService.getSprintById(id);
	}
	public List<Sprint> getAllSprints(){
		return sprintService.findAll();
	}

	//Markup keyboard
	public void markupKB(String username, Long chatId) {
		ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
		List<KeyboardRow> keyboard = new ArrayList<>();
		KeyboardRow row = new KeyboardRow();
		row.add(BotLabels.SHOW_MAIN_SCREEN.getLabel());
		Optional<TelegramUser> userOpt = telegramUserService.getUserbyAccount(username);
		// Check if the user exists
		if (userOpt.isPresent()) {
			TelegramUser user = userOpt.get();
			if ("Manager".equalsIgnoreCase(user.getRol())) {
				row.add(BotLabels.ADD_NEW_TASK.getLabel());
			} else if ("Developer".equalsIgnoreCase(user.getRol())) {
				row.add(BotLabels.CHECK_MY_TASKS.getLabel());
			}
		}
		
		keyboard.add(row);
		keyboardMarkup.setKeyboard(keyboard);
		SendMessage messageToTelegram = new SendMessage();
		messageToTelegram.setChatId(chatId);
		messageToTelegram.setReplyMarkup(keyboardMarkup);
		messageToTelegram.setText("Tools addes to your markup keyboard");
		try {
				execute(messageToTelegram);
		} catch (TelegramApiException e) {
			// Log the error
			logger.error("Error Markup Keyboard"+e.getLocalizedMessage(), e);
		}
	}


	//Prompts
	public void promptForUserInformation(long chatId) {
		SendMessage message = new SendMessage();
		message.setChatId(chatId);
		message.setText("Please enter your name:");
		try {
			execute(message);
			userStates.put(chatId, "WAITING_FOR_NAME");
		} catch (TelegramApiException e) {
			logger.error(e.getLocalizedMessage(), e);
		}	
	}
	
	public void promptForRole(long chatId) {
		SendMessage message = new SendMessage();
		message.setChatId(chatId);
		message.setText("Please select your role from the keyboard markup:");
	
		ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
		List<KeyboardRow> keyboard = new ArrayList<>();
		KeyboardRow row = new KeyboardRow();
		row.add("Manager");
		row.add("Developer");
		keyboardMarkup.setOneTimeKeyboard(true);
		keyboardMarkup.setResizeKeyboard(true); 
		keyboardMarkup.setSelective(true); 
		keyboard.add(row);
		keyboardMarkup.setKeyboard(keyboard);
		message.setReplyMarkup(keyboardMarkup);
	
		try {
			execute(message);
			userStates.put(chatId, "WAITING_FOR_ROLE");
		} catch (TelegramApiException e) {
			logger.error(e.getLocalizedMessage(), e);
		}		
	}
	
}
	
