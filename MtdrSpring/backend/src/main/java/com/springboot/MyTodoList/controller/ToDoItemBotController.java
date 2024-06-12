
// mvn spring-boot:run
package com.springboot.MyTodoList.controller;

import static org.mockito.ArgumentMatchers.booleanThat;
import static org.mockito.ArgumentMatchers.eq;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
// import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

import org.hibernate.internal.util.collections.ConcurrentReferenceHashMap.Option;
import org.hibernate.jdbc.Expectations;
import java.util.Set;
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
	private TeamService teamService;


	private String botName;
	private Map<Long, String> userStates = new HashMap<>();
	private Map<Long, TelegramUser> userMap = new HashMap<>();
	private Map<Long,Task> tempTasks = new HashMap<>();
	private Map<Long,Sprint> tempSprints = new HashMap<>();
	private Map<Long,List<Sprint>> tempSSprints = new HashMap<>();
	private Map<Long, Team> tempTeams = new HashMap<>();

	public ToDoItemBotController(String botToken, String botName, ToDoItemService toDoItemService, TelegramUserService telegramUserService,TaskService taskService,SprintService sprintService,TeamService teamService) {
		super(botToken);
		logger.info("Bot Token: " + botToken);
		logger.info("Bot name: " + botName);
		this.botName = botName;
		this.toDoItemService = toDoItemService;
		this.telegramUserService = telegramUserService;
		this.taskService = taskService;
		this.sprintService=sprintService;
		this.teamService=teamService;
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
					message = new SendMessage();
					message.setChatId(chatId);
					message.setText("Please enter your name:");
					try {
						execute(message);
						userStates.put(chatId, "WAITING_FOR_NAME");
						userMap.put(chatId,new TelegramUser());
					} catch (TelegramApiException e) {
						logger.error(e.getLocalizedMessage(), e);
					}					} else {
					try{
						SendMessage messageToTelegram = new SendMessage();
						messageToTelegram.setChatId(chatId);
						messageToTelegram.setText(BotMessages.HELLO_MYTODO_BOT.getMessage());
						execute(messageToTelegram);
						markupKB(user_username,chatId);
					}catch (Exception e){
						logger.error("Error: ", e);
					}
				}
			} else if (messageTextFromTelegram.equals(BotCommands.ADD_TASK.getCommand())
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

      }else if(messageTextFromTelegram.equals(BotCommands.CHECK_TASKS.getCommand())
				|| messageTextFromTelegram.equals(BotLabels.CHECK_MY_TASKS.getLabel())){
				try{
					Long id = telegramUserService.getUserbyAccount(user_username).get().getId();
					List<Task> tasks = taskService.getTasksByUserId(id);
					String tasksString = tasksToString(tasks); // Convert tasks to string

					SendMessage messageToTelegram = new SendMessage();
					messageToTelegram.setChatId(chatId);
					messageToTelegram.setText(tasksString); // Set the tasks string as the message text

					execute(messageToTelegram); // Send the message
				}catch (Exception e){
					logger.error("Error fetching tasks for user", e);

				}
			} else if(messageTextFromTelegram.equals(BotCommands.CREATE_SPRINT.getCommand())
			|| messageTextFromTelegram.equals(BotLabels.CREATE_SPRINT.getLabel())){
				// Optional<TelegramUser> userOpt = telegramUserService.getUserbyAccount(user_username);
				// if (userOpt.isPresent() && "Developer".equals(userOpt.get().getRol())) {
					SendMessage messageToTelegram = new SendMessage();
					messageToTelegram.setChatId(chatId);
					messageToTelegram.setText("Please enter the sprint title:");
					try {
						execute(messageToTelegram);
						userStates.put(chatId, "WAITING_FOR_SPRINT_TITLE");
					} catch (TelegramApiException e) {
						logger.error(e.getLocalizedMessage(), e);
					}
				// }
			} else if(messageTextFromTelegram.equals(BotCommands.VIEW_SPRINT_TASKS.getCommand())
			|| messageTextFromTelegram.equals(BotLabels.VIEW_SPRINT_TASKS.getLabel())){
				Optional<TelegramUser> userOpt = telegramUserService.getUserbyAccount(user_username);
				if(userOpt.get().getTeam().equals(null)){ // Si user no tiene un Team
					SendMessage messageToTelegram = new SendMessage();
					messageToTelegram.setChatId(chatId);
					messageToTelegram.setText("It seems like you don't have a Team. Please contact your Admin to assign one to you.\nYou must be in a Team to crete Sprints.");
					try {
						execute(messageToTelegram);
					} catch (TelegramApiException e) {
						logger.error(e.getLocalizedMessage(), e);
					}
				} else {
					try{
						Long teamId = userOpt.get().getTeam().getId();
						List<Sprint> sprints = sprintService.getTeamSprints(teamId);
						tempSSprints.put(chatId, sprints);
						String sprintlist = SprintListToStr(sprints); // Convert Sprint list to string
	
						SendMessage messageToTelegram = new SendMessage();
						messageToTelegram.setChatId(chatId);
						messageToTelegram.setText("Please select a Sprint: [enter a number]\n"+ sprintlist);
	
						try {
							execute(messageToTelegram);
							userStates.put(chatId, "WAITING_FOR_SPRINT_OPT");
						} catch (TelegramApiException e) {
							logger.error(e.getLocalizedMessage(), e);
						}
					}catch (Exception e){
						logger.error("Error fetching sprints for user", e);
					}
				}
			
			}else if(messageTextFromTelegram.equals(BotCommands.DELETE_SPRINT.getCommand())
			|| messageTextFromTelegram.equals(BotLabels.DELETE_SPRINT.getLabel())){
				Optional<TelegramUser> userOpt = telegramUserService.getUserbyAccount(user_username);
				try{
					Long teamId = userOpt.get().getTeam().getId();
					List<Sprint> sprints = sprintService.getTeamSprints(teamId);
					StringBuilder sb = new StringBuilder();
					for (int i=0; i < sprints.size(); i++) {
						Sprint s = sprints.get(i);
						sb.append("\n"+s.getId() + ": " + s.getTitle());
					}

					SendMessage messageToTelegram = new SendMessage();
					messageToTelegram.setChatId(chatId);
					messageToTelegram.setText("Please enter the sprint ID: \n"+sb.toString());
					try {
						execute(messageToTelegram);
						userStates.put(chatId, "WAITING_FOR_DEL_SPRINT_ID");
					} catch (TelegramApiException e) {logger.error(e.getLocalizedMessage(), e);}

				}catch (Exception e){logger.error("Error fetching sprints for user", e);}

      }else if (messageTextFromTelegram.equals(BotCommands.EDIT_TASK.getCommand())
		|| messageTextFromTelegram.equals(BotLabels.EDIT_TASK.getLabel())){
			Optional<TelegramUser> userOpt = telegramUserService.getUserbyAccount(user_username);
				if (userOpt.isPresent() && "Developer".equals(userOpt.get().getRol())) {
					List<Task> tasks = getTaskbyCurrentSprint(userOpt.get());
					
					String textTasks = "";
					for(Task task:tasks){
						textTasks += task.toString()+"\n";						
					}
					SendMessage messageToTelegram = new SendMessage();
					if (textTasks.isEmpty()) {
						messageToTelegram.setText("There are no tasks to edit");
					}else{
						messageToTelegram.setText(textTasks + "\nSelect a task id, from your tasks.");
					}
					messageToTelegram.setChatId(chatId);
					try {
						execute(messageToTelegram);
						userStates.put(chatId, "WAITING_FOR_EDIT_TASK_ID");
					}catch (TelegramApiException e) {
							logger.error(e.getLocalizedMessage(), e);
						}
				}else{
					//Get all manager teams task
					
					//Get current sprint

					//Get all tasks from current sprint


					SendMessage messageToTelegram = new SendMessage();
					messageToTelegram.setText("Select a task id, from your team's tasks.");
					messageToTelegram.setChatId(chatId);
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
				if (t.isEmpty()) {
					messageToTelegram.setText("You have no tasks");
				} else {
					messageToTelegram.setText(t);
				}
				execute(messageToTelegram);
			}catch (Exception e){
				logger.error("Error fetching tasks for user", e);

			}
		}else if (messageTextFromTelegram.equals(BotCommands.EDIT_USER.getCommand())
		|| messageTextFromTelegram.equals(BotLabels.EDIT_USER.getLabel())){
			Optional<TelegramUser> user =telegramUserService.getUserbyAccount(user_username);
			SendMessage messageToTelegram = new SendMessage();
			messageToTelegram.setChatId(chatId);
			if (user.isPresent()){
				userMap.put(chatId,user.get());
				messageToTelegram.setText("Excelent, answer the questions again.\nPlease enter your name:");
				userStates.put(chatId,"WAITING_FOR_NAME");
			}else{
				messageToTelegram.setText("Ooops it seems you dont have a user. Please register");

			}
			
			try{
				execute(messageToTelegram);
			}catch (Exception e){
				logger.error("Error: ", e);
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
				
				ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
				keyboardMarkup.setKeyboard(keyboardRows);
				keyboardMarkup.setOneTimeKeyboard(true); 
				keyboardMarkup.setResizeKeyboard(true); 
				keyboardMarkup.setSelective(true); 
				
				messageToTelegram.setReplyMarkup(keyboardMarkup);
				if(t.isEmpty()){
					messageToTelegram.setText("You have no tasks.");
				}else{
					messageToTelegram.setText(t+"\nSelect a task to finish from the markup keyboard:");
				}
				execute(messageToTelegram);
				userStates.put(chatId,"WAITING_FOR_COMPLETED_TASK");
			}catch (Exception e){
				logger.error(e.getLocalizedMessage(), e);
			}
			
		}else if(messageTextFromTelegram.equals(BotCommands.CREATE_TEAM.getCommand()) 
			|| messageTextFromTelegram.equals(BotLabels.CREATE_TEAM.getLabel())){
			Optional<TelegramUser> userOpt = telegramUserService.getUserbyAccount(user_username);
			if (userOpt.isPresent() && "Manager".equals(userOpt.get().getRol())) {
				SendMessage messageToTelegram = new SendMessage();
				messageToTelegram.setChatId(chatId);
				messageToTelegram.setText("Please enter the team name:");
				try {
					execute(messageToTelegram);
					userStates.put(chatId, "WAITING_FOR_TEAM_NAME");
					tempTeams.put(chatId, new Team());
				} catch (TelegramApiException e) {
					logger.error(e.getLocalizedMessage(), e);
				}
			}
		}else if(messageTextFromTelegram.equals(BotCommands.DELETE_TEAM.getCommand())
			|| messageTextFromTelegram.equals(BotLabels.DELETE_TEAM.getLabel())){
			Optional<TelegramUser> userOpt = telegramUserService.getUserbyAccount(user_username);
			if (userOpt.isPresent() && "Manager".equals(userOpt.get().getRol())) {
				SendMessage messageToTelegram = new SendMessage();
				messageToTelegram.setChatId(chatId);
				messageToTelegram.setText("Please enter the team ID:");
				try {
					execute(messageToTelegram);
					userStates.put(chatId, "WAITING_FOR_TEAM_ID");
				} catch (TelegramApiException e) {
					logger.error(e.getLocalizedMessage(), e);
				}
			}
		}else if(messageTextFromTelegram.equals(BotCommands.VIEW_TEAM_TASKS.getCommand())
			|| messageTextFromTelegram.equals(BotLabels.VIEW_TEAM_TASKS.getLabel())){
			Optional<TelegramUser> userOpt = telegramUserService.getUserbyAccount(user_username);
			if (userOpt.isPresent() && "Manager".equals(userOpt.get().getRol())) {
				SendMessage messageToTelegram = new SendMessage();
				messageToTelegram.setChatId(chatId);
				messageToTelegram.setText("Please enter the team ID:");
				try {
					execute(messageToTelegram);
					userStates.put(chatId, "WAITING_FOR_TEAM_ID_TASKS");
				} catch (TelegramApiException e) {
					logger.error(e.getLocalizedMessage(), e);
				}
			}
		}else if(messageTextFromTelegram.equals(BotCommands.EDIT_TEAM.getCommand())
			|| messageTextFromTelegram.equals(BotLabels.EDIT_TEAM.getLabel())){
			Optional<TelegramUser> userOpt = telegramUserService.getUserbyAccount(user_username);
			if (userOpt.isPresent() && "Manager".equals(userOpt.get().getRol())) {
				SendMessage messageToTelegram = new SendMessage();
				messageToTelegram.setChatId(chatId);
				messageToTelegram.setText("Please enter the team ID:");
				try {
					execute(messageToTelegram);
					userStates.put(chatId, "WAITING_FOR_TEAM_ID_EDIT");
				} catch (TelegramApiException e) {
					logger.error(e.getLocalizedMessage(), e);
				}
			}
		}else if(messageTextFromTelegram.equals(BotCommands.VIEW_TEAM_MEMBERS.getCommand())
			|| messageTextFromTelegram.equals(BotLabels.VIEW_TEAM_MEMBERS.getLabel())){
			Optional<TelegramUser> userOpt = telegramUserService.getUserbyAccount(user_username);
			if (userOpt.isPresent() && "Manager".equals(userOpt.get().getRol())) {
				SendMessage messageToTelegram = new SendMessage();
				messageToTelegram.setChatId(chatId);
				messageToTelegram.setText("Please enter the team ID:");
				try {
					execute(messageToTelegram);
					userStates.put(chatId, "WAITING_FOR_TEAM_ID_MEMBERS");
				} catch (TelegramApiException e) {
					logger.error(e.getLocalizedMessage(), e);
				}
			}

		}
		
		else{
			if (userStates.get(chatId).equals("WAITING_FOR_NAME")) {
				TelegramUser telegramUser = userMap.get(chatId);
				telegramUser.setName(messageTextFromTelegram);
				telegramUser.setAccount(user_username);
				userMap.put(chatId,telegramUser);
				message = new SendMessage();
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
			}else if (userStates.get(chatId).equals("WAITING_FOR_ROLE")) {
				try {
					TelegramUser newTelegramUser = userMap.get(chatId);
					newTelegramUser.setRol(messageTextFromTelegram);
					ResponseEntity entity = saveUser(newTelegramUser,chatId);		
				
					SendMessage messageToTelegram = new SendMessage();
					userMap.put(chatId,null);
					userStates.put(chatId, null);
					messageToTelegram = new SendMessage();
					messageToTelegram.setChatId(chatId);
					messageToTelegram.setText("User saved correctly!");
					execute(messageToTelegram);				
					markupKB(user_username,chatId);
				} catch (Exception e) {
					logger.error(e.getLocalizedMessage(), e);
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
			

				// Create SPRINT
			}else if(userStates.get(chatId).equals("WAITING_FOR_SPRINT_TITLE")){
					Sprint tempSprint = new Sprint();
					tempSprint.setTitle(messageTextFromTelegram);
					// tempSprint.setStatus("NotStarted");
					Optional<TelegramUser> assigned = telegramUserService.getUserbyAccount(user_username);
					if(assigned.isPresent()){
						// tempSprint.setUser(assigned.get());
						tempSprints.put(chatId, tempSprint);

						SendMessage messageToTelegram = new SendMessage();
						messageToTelegram.setChatId(chatId);
						messageToTelegram.setText("Please enter the sprint status:");
						try {
							execute(messageToTelegram);
							userStates.put(chatId, "WAITING_FOR_SPRINT_STATUS");
						} catch (TelegramApiException e) {
							logger.error(e.getLocalizedMessage(), e);
						}
					}else{
						SendMessage messageToTelegram = new SendMessage();
						messageToTelegram.setChatId(chatId);
						messageToTelegram.setText("An error has occured");
						try {
							execute(messageToTelegram);
						} catch (TelegramApiException e) {
							logger.error(e.getLocalizedMessage(), e);
						}
					}
			}else if(userStates.get(chatId).equals("WAITING_FOR_SPRINT_STATUS")){
					Sprint tempSprint = tempSprints.get(chatId);
					tempSprint.setStatus(messageTextFromTelegram);
					tempSprints.put(chatId, tempSprint);

					SendMessage messageToTelegram = new SendMessage();
					messageToTelegram.setChatId(chatId);
					messageToTelegram.setText("Please enter the sprint Start date\nformat: aaaa-dd-mm");
					try {
						execute(messageToTelegram);
						userStates.put(chatId, "WAITING_FOR_SPRINT_STARTDATE");
					} catch (TelegramApiException e) {
						logger.error(e.getLocalizedMessage(), e);
					}
			//States

			}else if(userStates.get(chatId).equals("WAITING_FOR_SPRINT_STARTDATE")){
					Sprint tempSprint = tempSprints.get(chatId);
					Timestamp ts = strToTimestamp(messageTextFromTelegram);
					if(ts.equals(null)){
						logger.error("EL mensaje n ocuadra con el formato de fecha");
						SendMessage messageToTelegram = new SendMessage();
						messageToTelegram.setChatId(chatId);
						messageToTelegram.setText("Error: invalid date. PLease enter Start date again.\nformat: aaaa-dd-mm");
						try{
							execute(messageToTelegram);
						}catch(TelegramApiException e){
							logger.error("Error en mensaje recibido");
						}
					} else {
						tempSprint.setStartDate(ts);
						tempSprints.put(chatId, tempSprint);

						SendMessage messageToTelegram = new SendMessage();
						messageToTelegram.setChatId(chatId);
						messageToTelegram.setText("Please enter the sprint End date\nformat: aaaa-dd-mm:");
						try {
							execute(messageToTelegram);
							userStates.put(chatId, "WAITING_FOR_SPRINT_ENDDATE");
						} catch (TelegramApiException e) {
							logger.error(e.getLocalizedMessage(), e);
						}
					}
			}else if(userStates.get(chatId).equals("WAITING_FOR_SPRINT_ENDDATE")){
					Sprint tempSprint = tempSprints.get(chatId);
					Timestamp ts = strToTimestamp(messageTextFromTelegram);
					if(ts.equals(null)){
						SendMessage messageToTelegram = new SendMessage();
						messageToTelegram.setChatId(chatId);
						messageToTelegram.setText("Error: invalid date. PLease enter Start date again.\nformat: aaaa-dd-mm");
						try{
							execute(messageToTelegram);
						}catch(TelegramApiException e){
							logger.error("Error en mensaje recibido");
						}
					} else {
						tempSprint.setEndDate(ts);
						tempSprints.put(chatId, tempSprint);

						Optional<TelegramUser> userOpt = telegramUserService.getUserbyAccount(user_username);
						if (userOpt.isPresent() && "Manager".equals(userOpt.get().getRol())) {
							SendMessage messageToTelegram = new SendMessage();
							messageToTelegram.setChatId(chatId);
							messageToTelegram.setText("Please enter Team ID:");
							try {
								execute(messageToTelegram);
								userStates.put(chatId, "WAITING_FOR_SPRINT_TEAMID"); // Solo Manager puede llegar a este estado
							} catch (TelegramApiException e) {
								logger.error(e.getLocalizedMessage(), e);
							}
						} else {
							Team t = userOpt.get().getTeam();
							tempSprint.setTeamID(t);
							sprintService.addSprint(tempSprint); // Save the Sprint to the database
							logger.info("Sprint created");						
							
							tempSprints.put(chatId,null); // Remove the temp task

							SendMessage messageToTelegram = new SendMessage();
							messageToTelegram.setChatId(chatId);
							messageToTelegram.setText("Sprint added successfully!");
							userStates.put(chatId, null);
							try {
								execute(messageToTelegram);
							} catch (TelegramApiException e) {
								logger.error(e.getLocalizedMessage(), e);
							}
						}

					}
			}else if(userStates.get(chatId).equals("WAITING_FOR_SPRINT_TEAMID")){
					Sprint tempSprint = tempSprints.get(chatId);
					Optional<Team> t = teamService.getTeamById(Long.parseLong(messageTextFromTelegram));
					if(t.isPresent()){
						tempSprint.setTeamID(t.get());
						sprintService.addSprint(tempSprint); // Save the Sprint to the database
						logger.info("Sprint created");

						tempSprints.put(chatId,null); // Remove the temp task

						SendMessage messageToTelegram = new SendMessage();
						messageToTelegram.setChatId(chatId);
						messageToTelegram.setText("Sprint added successfully!");
						userStates.put(chatId, null);
						try {
							execute(messageToTelegram);
						} catch (TelegramApiException e) {
							logger.error(e.getLocalizedMessage(), e);
						}
					} else {
						SendMessage messageToTelegram = new SendMessage();
						messageToTelegram.setChatId(chatId);
						messageToTelegram.setText("Error: Invalid Team ID");
						try{
							execute(message);
						}catch(TelegramApiException e){
							logger.error("Error en mensaje recibido");
						}
					}
			}else if(userStates.get(chatId).equals("WAITING_FOR_SPRINT_OPT")){
					if(isInt(messageTextFromTelegram)){
						Integer opt = Integer.parseInt(messageTextFromTelegram); // selected Sprint number (index)
						List<Sprint> sprintList = tempSSprints.get(chatId);
						if(opt<=sprintList.size() && opt>0){
							Optional<TelegramUser> userOpt = telegramUserService.getUserbyAccount(user_username);
							StringBuilder sb = new StringBuilder();
							StringBuilder sb2 = new StringBuilder();
							Sprint sprint = sprintList.get(opt-1);
							sb.append("Sprint: "+sprint.getTitle()+"\nStatus: "+sprint.getStatus()+"\nStart: "+sprint.getStartDate()+"\nEnd: "+sprint.getEndDate()+"\n\n");

							if (userOpt.isPresent() && "Manager".equals(userOpt.get().getRol())) {
								// Manager puede ver todos los tasks del Sprint del equipo
								try{
									List<TelegramUser> teamUsers = telegramUserService.getTeamUsers(userOpt.get().getTeam().getId());

									for(int j=0; j<teamUsers.size(); j++){
										StringBuilder sb3 = new StringBuilder();

										List<Task> tasks = taskService.getTasksByUserId(teamUsers.get(j).getId());
										for(Integer i=0; i<tasks.size(); i++){
											if(tasks.get(i).getSprint().getId().equals(sprint.getId())){
												Task t = tasks.get(i);
												sb3.append(t.getDescription()+" - "+t.getStatus()+"\n");
											}
										}
										if (sb3.length() == 0) {
											sb3.append("No tasks added for this Sprint");
										}
										sb2.append(teamUsers.get(j).getName()+":\n"+sb3.toString()+"\n\n");
									}
								} catch (Exception e){
									logger.error("Error fetching sprints for user", e);
								}

							} else { // Para Dev
								// Dev solo puede ver sus tasks del Sprint
								try{
									List<Task> tasks = taskService.getTasksByUserId(userOpt.get().getId());
									for(Integer i=0; i<tasks.size(); i++){
										if(tasks.get(i).getSprint().getId().equals(sprint.getId())){
											Task t = tasks.get(i);
											sb2.append(t.getDescription()+" - "+t.getStatus()+"\n");
										}
									}
									if (sb2.length() == 0) {
										sb2.append("No tasks added for this Sprint");
									}
								}catch (Exception e){
									logger.error("Error fetching sprints for user", e);
								}
							}
							SendMessage messageToTelegram = new SendMessage();
							messageToTelegram.setChatId(chatId);
							messageToTelegram.setText(sb.toString()+sb2.toString());
							try {execute(messageToTelegram);} catch (TelegramApiException e) {logger.error(e.getLocalizedMessage(), e);}
						}else{
							SendMessage messageToTelegram = new SendMessage();
							messageToTelegram.setChatId(chatId);
							messageToTelegram.setText("Please select a number from the Sprint list");
							try {execute(messageToTelegram);} catch (TelegramApiException e) {logger.error(e.getLocalizedMessage(), e);}
						}
					} else {
						SendMessage messageToTelegram = new SendMessage();
						messageToTelegram.setChatId(chatId);
						messageToTelegram.setText("Please enter a number");
						try {execute(messageToTelegram);} catch (TelegramApiException e) {logger.error(e.getLocalizedMessage(), e);}
					}
				}else if(userStates.get(chatId).equals("WAITING_FOR_DEL_SPRINT_ID")){
					try {
						Long sprintId = Long.parseLong(messageTextFromTelegram);
						Optional<Sprint> sprint = getSprintfromId(sprintId);
						if (sprint.isPresent()) {
							sprintService.deleteSprint(sprintId);
							userStates.put(chatId, null);
							SendMessage messageToTelegram = new SendMessage();
							messageToTelegram.setChatId(chatId);
							messageToTelegram.setText("Sprint deleted successfully!");
							execute(messageToTelegram);
						} else {
							SendMessage messageToTelegram = new SendMessage();
							messageToTelegram.setChatId(chatId);
							messageToTelegram.setText("Invalid Sprint ID. Try again");
							execute(messageToTelegram);
						}
					} catch (NumberFormatException e) {
						logger.error("Invalid sprint ID format: " + messageTextFromTelegram);
						// Send a message indicating that the sprint ID format is invalid
					} catch (TelegramApiException e) {
						logger.error("Error in delete"+e.getLocalizedMessage(), e);
					}
			//States de main
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

			}else if(userStates.get(chatId).equals("WAITING_FOR_EDIT_TASK_ID")){
				try {
					Long taskId = Long.parseLong(messageTextFromTelegram);
					Optional<Task> task = taskService.findById(taskId);
					if(task.isPresent()){
						tempTasks.put(chatId, task.get());
						SendMessage messageToTelegram = new SendMessage();
						messageToTelegram.setChatId(chatId);
						messageToTelegram.setText("Enter new task description:");
						execute(messageToTelegram);
						userStates.put(chatId, "WAITING_FOR_TASK_DESCRIPTION");
					}else{
						SendMessage messageToTelegram = new SendMessage();
						messageToTelegram.setChatId(chatId);
						messageToTelegram.setText("Wrong task id");
						execute(messageToTelegram);

					}
				}catch (Exception e){
					logger.error(e.getLocalizedMessage(), e);
				}
			}else if(userStates.get(chatId).equals("WAITING_FOR_TEAM_ID_TASKS")){
				try{
					Long teamId = Long.parseLong(messageTextFromTelegram);
					Optional<Team> team = teamService.getTeamById(teamId);
					if(team.isPresent()){
						List<TelegramUser> members = telegramUserService.getUsersByTeamID(teamId);
						List<Task> tasks;
						for(TelegramUser member : members){
							tasks = taskService.getTasksByUserId(member.getId()); 
							String tasksToString = tasksToString(tasks); // Convert tasks to string
							SendMessage messageToTelegram = new SendMessage();
							messageToTelegram.setChatId(chatId);
							messageToTelegram.setText(tasksToString); // Set the users string as the message text
							try{
								execute(messageToTelegram); // Send the message
							}catch(TelegramApiException e){
								logger.error(e.getLocalizedMessage(), e);
							}
						}
					}else{
						SendMessage messageToTelegram = new SendMessage();
						messageToTelegram.setChatId(chatId);
						messageToTelegram.setText("Invalid team ID");
						try {
							execute(messageToTelegram);
						} catch (TelegramApiException e) {
							logger.error(e.getLocalizedMessage(), e);
						}
					}
				}catch(NumberFormatException e){
					logger.error("Invalid team ID format: " + messageTextFromTelegram);
					// Send a message indicating that the team ID format is invalid
				}
			}
		}}
	}
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

	public ResponseEntity createTeam(@RequestBody Team team, long chatId){
		try{
			Team savedTeam = teamService.addTeam(tempTeams.get(chatId));
			if(savedTeam != null){
				HttpHeaders responseHeaders = new HttpHeaders();
				responseHeaders.set("location", "" + savedTeam.getId());
				responseHeaders.set("Access-Control-Expose-Headers", "location");
				return ResponseEntity.ok().headers(responseHeaders).build();
			}else{
				throw new IllegalArgumentException("No team saved");
			}
		}catch (Exception e){
			// Log the exception or handle it as needed
			System.err.println("Error saving Team: " + e.getMessage());
			// Return a 500 Internal Server Error
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving Team");
		}
	}

	public ResponseEntity deleteTeam(@RequestBody Team team){
		try{
			teamService.deleteTeam(team.getId());
		}catch (Exception e){
			// Log the exception or handle it as needed
			System.err.println("Error saving Team: " + e.getMessage());
			// Return a 500 Internal Server Error
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting Team");
		}
		return ResponseEntity.ok().build();
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
	public List<Team> getAllTeams(){
		return teamService.findAll();
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
				row.add(BotLabels.CREATE_TEAM.getLabel());
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

	// String to Timestamp format
	public Timestamp strToTimestamp(String dateString) {
		// String dateString = "2024-01-26 12:30:45";
		dateString = dateString + " 00:00:00";
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			java.util.Date parsedDate = dateFormat.parse(dateString);
			Timestamp timestamp = new Timestamp(parsedDate.getTime());
			// System.out.println("Input String: " + dateString +" And It data type is"+ dateString.getClass());
			// System.out.println("Converted Timestamp: "+ timestamp +" And It data type is "+ timestamp.getClass());
			return timestamp;
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	// List<String> to String
	public String ListStrToStr(List<String> lst) {
		StringBuilder sb = new StringBuilder();
		if (lst.isEmpty()) {
			sb.append("You have no tasks :)").append("\n");
		} else {
			for (String s : lst) {
				sb.append(s).append("\n"); 
			}
		}
		return sb.toString();
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

	// List of Sprints to String names
	public String SprintListToStr(List<Sprint> sprintList) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i=0; i < sprintList.size(); i++) {
            stringBuilder.append((i + 1) + ": " + sprintList.get(i).getTitle());
            if (i < sprintList.size() - 1) {
                stringBuilder.append("\n"); // Agregar un salto de línea si no es el último elemento
            }
        }
        return stringBuilder.toString();
    }

	// Validarque un mensaje pueda convertirse a Int
	public boolean isInt(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


	//Team Prompts
	public void promptForTeamInformation(long chatId) {
		SendMessage message = new SendMessage();
		message.setChatId(chatId);
		message.setText("Please enter the team name:");
		try {
			execute(message);
			userStates.put(chatId, "WAITING_FOR_TEAM_NAME");
		} catch (TelegramApiException e) {
			logger.error(e.getLocalizedMessage(), e);
		}	
	}

	public void promptForTeamId(long chatId) {
		SendMessage message = new SendMessage();
		message.setChatId(chatId);
		message.setText("Please enter the team ID:");
		try {
			execute(message);
			userStates.put(chatId, "WAITING_FOR_TEAM_ID");
		} catch (TelegramApiException e) {
			logger.error(e.getLocalizedMessage(), e);
		}	
	}

	private String tasksToString(List<Task> tasks) {
		StringBuilder sb = new StringBuilder();
		if (tasks.isEmpty()) {
			sb.append("You have no tasks :)").append("\n");
		} else {
			for (Task task : tasks) {
				sb.append(task.getDescription()).append("\n"); // Assuming Task has a getDescription method
			}
		}
		return sb.toString();
	}

	private String usersToString(List<TelegramUser> users) {
		StringBuilder sb = new StringBuilder();
		if (users.isEmpty()) {
			sb.append("There are no users :)").append("\n");
		} else {
			for (TelegramUser user : users) {
				sb.append(user.getName()).append(", Role: ").append(user.getRol()).append("\n"); // Assuming TelegramUser has a getName method
			}
		}
		return sb.toString();
	}
    public List<Task> getTaskbyCurrentSprint(TelegramUser user){
		List<Sprint> curreSprints = sprintService.getCurrentSprint(user.getTeam().getId());
		Set<Long> currentSprintIds = curreSprints.stream().map(Sprint::getId).collect(Collectors.toSet());

		List <Task> userTasks = taskService.getTasksByUserId(user.getId());
		List<Task> tasksInSprint = new ArrayList<>();
		for (Task task:userTasks){
			if (currentSprintIds.contains(task.getSprint().getId())) {
				tasksInSprint.add(task);
			}
		}
		return tasksInSprint;
	}	
	
}
