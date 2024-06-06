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

	public ToDoItemBotController(String botToken, String botName, ToDoItemService toDoItemService, TelegramUserService telegramUserService,TaskService taskService,SprintService sprintService,TeamService teamService) {
		super(botToken);
		logger.info("Bot Token: " + botToken);
		logger.info("Bot name: " + botName);
		this.botName = botName;
		this.toDoItemService = toDoItemService;
		this.telegramUserService=telegramUserService;
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
						markupKB(user_username);
					}catch (Exception e){

					}
				}
			} else if (messageTextFromTelegram.equals(BotCommands.ADD_TASK.getCommand())
				|| messageTextFromTelegram.equals(BotLabels.ADD_NEW_TASK.getLabel())){
					Optional<TelegramUser> userOpt = telegramUserService.getUserbyAccount(user_username);
					if (userOpt.isPresent() && "Developer".equals(userOpt.get().getRol())) {
						SendMessage messageToTelegram = new SendMessage();
						messageToTelegram.setChatId(chatId);
						messageToTelegram.setText("Please enter the task description:");
						try {
							execute(messageToTelegram);
							userStates.put(chatId, "WAITING_FOR_TASK_DESCRIPTION");
						} catch (TelegramApiException e) {
							logger.error(e.getLocalizedMessage(), e);
						}
					}
			}else if (messageTextFromTelegram.equals(BotCommands.CHECK_TASKS.getCommand())
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
							// userStates.put(chatId, "WAITING_FOR_SPRINT_OPT");
						} catch (TelegramApiException e) {
							logger.error(e.getLocalizedMessage(), e);
						}
					}catch (Exception e){
						logger.error("Error fetching sprints for user", e);
					}
				}
			
			}else if(messageTextFromTelegram.equals(BotCommands.DELETE_SPRINT.getCommand())
			|| messageTextFromTelegram.equals(BotLabels.DELETE_SPRINT.getLabel())){
				SendMessage messageToTelegram = new SendMessage();
				messageToTelegram.setChatId(chatId);
				messageToTelegram.setText("Please enter the sprint ID:");
				try {
					execute(messageToTelegram);
					userStates.put(chatId, "WAITING_FOR_DEL_SPRINT_ID");
				} catch (TelegramApiException e) {
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
						markupKB(user_username);
					} catch (Exception e) {
						logger.error(e.getLocalizedMessage(), e);
					}

				// TASK
				}else if(userStates.get(chatId).equals("WAITING_FOR_TASK_DESCRIPTION")){
					Task tempTask = new Task();
					tempTask.setDescription(messageTextFromTelegram);
					tempTask.setStatus("NotStarted");
					Optional<TelegramUser> assigned = telegramUserService.getUserbyAccount(user_username);
					if(assigned.isPresent()){
						tempTask.setUser(assigned.get());
						tempTasks.put(chatId, tempTask);

						SendMessage messageToTelegram = new SendMessage();
						messageToTelegram.setChatId(chatId);
						messageToTelegram.setText("Please enter the user ID:");
						try {
							execute(messageToTelegram);
							userStates.put(chatId, "WAITING_FOR_SPRINT_ASSIGN");
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
				}else if(userStates.get(chatId).equals("WAITING_FOR_SPRINT_ASSIGN")){
					Task tempTask = tempTasks.get(chatId);
					try {
						Long sprintId = Long.parseLong(messageTextFromTelegram);
						// Optional<Sprint> sprint = getSprintfromId(sprintId);
						List<Sprint> sprints = getAllSprints();
						if (sprints.isEmpty()){
							SendMessage messageToTelegram = new SendMessage();
							messageToTelegram.setChatId(chatId);
							messageToTelegram.setText("Invalid Sprint. Try again");
							execute(messageToTelegram);

						}else{
							tempTask.setSprint(sprints.get(0));

							taskService.saveTask(tempTask); // Save the task to the database
							logger.info("Task created");

							tempTasks.put(chatId,null); // Remove the temp task

							userStates.put(chatId, null);// Quitar el estado del usuario
							SendMessage messageToTelegram = new SendMessage();
							messageToTelegram.setChatId(chatId);
							messageToTelegram.setText("Task added successfully!");
							execute(messageToTelegram);
						}
					} catch (NumberFormatException e) {
						logger.error("Invalid sprint ID format: " + messageTextFromTelegram);
						// Send a message indicating that the sprint ID format is invalid
					} catch (TelegramApiException e) {
						logger.error("Error in assign"+e.getLocalizedMessage(), e);
					}
				}

				// Create SPRINT
				else if(userStates.get(chatId).equals("WAITING_FOR_SPRINT_TITLE")){
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
				} else if(userStates.get(chatId).equals("WAITING_FOR_SPRINT_STATUS")){
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

				} else if(userStates.get(chatId).equals("WAITING_FOR_SPRINT_STARTDATE")){
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
				}

				else if(userStates.get(chatId).equals("WAITING_FOR_SPRINT_ENDDATE")){
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
				} else if(userStates.get(chatId).equals("WAITING_FOR_SPRINT_TEAMID")){
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
				}
				// View SPRINT (Current)
				// WAITING_FOR_SPRINT_VIEW_OPT}
				// Delete SPRINT
				else if(userStates.get(chatId).equals("WAITING_FOR_DEL_SPRINT_ID")){
					try {
						Long sprintId = Long.parseLong(messageTextFromTelegram);
						Optional<Sprint> sprint = getSprintfromId(sprintId);
						if (sprint.isPresent()) {
							sprintService.deleteSprint(sprintId);
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
				}
			}
		}
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
	public void markupKB(String username) {
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
		messageToTelegram.setReplyMarkup(keyboardMarkup);

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

}
