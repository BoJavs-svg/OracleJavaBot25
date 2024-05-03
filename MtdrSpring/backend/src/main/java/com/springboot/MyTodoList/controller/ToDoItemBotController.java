// Latest: this one (8)
// mvn spring-boot:run
package com.springboot.MyTodoList.controller;

// import static org.junit.jupiter.api.Assertions.assertEquals;

// import static org.mockito.ArgumentMatchers.booleanThat; No sé para qué son estas líneas, las tenía el Javier
// import static org.mockito.ArgumentMatchers.eq;

// import java.text.ParseException; Descomentar al utilizar String->Date
// import java.text.SimpleDateFormat;

// import java.time.OffsetDateTime;
// import java.time.OffsetDateTime;
// import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
// import java.util.stream.Collectors;
// import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.context.ApplicationContext;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
// import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.springboot.MyTodoList.model.ToDoItem;
import com.springboot.MyTodoList.model.Sprint;
import com.springboot.MyTodoList.model.Team;
import com.springboot.MyTodoList.model.TelegramUser;

import com.springboot.MyTodoList.service.ToDoItemService;
import com.springboot.MyTodoList.service.TeamService;
import com.springboot.MyTodoList.service.SprintService;
import com.springboot.MyTodoList.service.TelegramUserService;
import com.springboot.MyTodoList.util.BotCommands;
import com.springboot.MyTodoList.util.BotHelper;
import com.springboot.MyTodoList.util.BotLabels;
import com.springboot.MyTodoList.util.BotMessages;

import java.util.Map;
// import java.util.HashMap;


public class ToDoItemBotController extends TelegramLongPollingBot {

	private static final Logger logger = LoggerFactory.getLogger(ToDoItemBotController.class);
	@Autowired
	private ToDoItemService toDoItemService;
	@Autowired
	private TelegramUserService telegramUserService;

	private String botName;
	private Map<Long, String> userStates = new HashMap<>();
	private Map<Long, TelegramUser> userMap = new HashMap<>();	
	
	public ToDoItemBotController(String botToken, String botName, ToDoItemService toDoItemService, TelegramUserService telegramUserService) {
		super(botToken);
		logger.info("Bot Token: " + botToken);
		logger.info("Bot name: " + botName);
		this.toDoItemService = toDoItemService;
		this.telegramUserService=telegramUserService;
		this.botName = botName;
	}

	@Override
	public void onUpdateReceived(Update update) {
		if (update.hasMessage() && update.getMessage().hasText()) {
			String messageTextFromTelegram = update.getMessage().getText();
			long chatId = update.getMessage().getChatId();
			if (!userStates.containsKey(chatId)) {
         		   userStates.put(chatId, null); // Initialize state for new user
        		}
			logger.info("I got a letter from ("+chatId+"): " + messageTextFromTelegram);
			SendMessage message = new SendMessage();
			message.setChatId(chatId);
			message.setText("Mary?"); // message.setText("Mary? " + messageTextFromTelegram);
			try{
				execute(message);
			}catch(TelegramApiException e){
				logger.error("Error en mensaje recibido");
			}
			// start
			if (messageTextFromTelegram.equals(BotCommands.START_COMMAND.getCommand())
					|| messageTextFromTelegram.equals(BotLabels.SHOW_MAIN_SCREEN.getLabel())) {
				ResponseEntity<Boolean> response = findIfExists(chatId);
				if (!response.getBody()) {
					message = new SendMessage();
					message.setChatId(chatId);
					message.setText("Who are you? [Please enter a name]");
					try{
						execute(message);
					}catch(TelegramApiException e){
						logger.error("Error en mensaje recibido");
					}
					promptForUserInformation(chatId);
				} else {
					SendMessage messageToTelegram = new SendMessage();
					messageToTelegram.setChatId(chatId);
					messageToTelegram.setText(BotMessages.HELLO_MYTODO_BOT.getMessage());
			
					ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
					List<KeyboardRow> keyboard = new ArrayList<>();
			
					// first row
					KeyboardRow row = new KeyboardRow();
					row.add(BotLabels.LIST_ALL_ITEMS.getLabel());
					row.add(BotLabels.ADD_NEW_ITEM.getLabel());
					// Add the first row to the keyboard
					keyboard.add(row);
			
					// second row
					row = new KeyboardRow();
					row.add(BotLabels.SHOW_MAIN_SCREEN.getLabel());
					row.add(BotLabels.HIDE_MAIN_SCREEN.getLabel());
					keyboard.add(row);

					// 3
					row = new KeyboardRow();
					row.add(BotLabels.ADD_NEW_SPRINT.getLabel());
					keyboard.add(row);
			
					// Set the keyboard
					keyboardMarkup.setKeyboard(keyboard);
			
					// Add the keyboard markup
					messageToTelegram.setReplyMarkup(keyboardMarkup);
			
					try {
						execute(messageToTelegram);
					} catch (TelegramApiException e) {
						logger.error(e.getLocalizedMessage(), e);
					}
				}
			// Sprint (call)
			} else if(messageTextFromTelegram.equals(BotCommands.ADD_SPRINT.getCommand())
				   || messageTextFromTelegram.equals(BotLabels.ADD_NEW_SPRINT.getLabel())) {
				SendMessage messageToTelegram = new SendMessage();
				messageToTelegram.setChatId(chatId);
				messageToTelegram.setText(BotMessages.NEW_SPRINT_CREATED.getMessage());

				// Change userStates for current state (/sprint called)
				userStates.put(chatId, "SPRINT_CALLED");

				try { // Contestar
					execute(messageToTelegram);
				} catch (TelegramApiException e) {
					logger.error(e.getLocalizedMessage(), e);
				}
			
			// Sprint (wait for sprint content)
			} else if(userStates.get(chatId).equals("SPRINT_CALLED")){
				// Sprint will use only one message to receive the 5 parameters
				String regex = "\\[([^\\]]+)\\]";

				java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
				java.util.regex.Matcher matcher = pattern.matcher(messageTextFromTelegram);

				String title = null, status = null, startDate = null, endDate = null;
				Long teamLong = null;
				TeamService TS = new TeamService(null);
				Team teamID = new Team();
				SprintController sprintController = new SprintController();

				int index = 0;
				while (matcher.find()) {
					String match = matcher.group(1);
					switch (index) {
						case 0:
							title = match;
							break;
						case 1:
							status = match;
							break;
						case 2:
							startDate = match; //startDateStr
							break;
						case 3:
							endDate = match; // endDateStr
							break;
						case 4:
							teamLong = Long.parseLong(match);
							teamID = TS.getTeamById(teamLong);
							break;
						default:
							SendMessage messageToTelegra = new SendMessage();
							messageToTelegra.setChatId(chatId);
							messageToTelegra.setText(BotMessages.SPRINT_SYN_ERROR.getMessage());		
							break;
					}
					index++;
				}

				// D e s p r e c i a d o. Se cambiaron los tipos Date por String
				// Date startDate=null;
				// Date endDate=null;
				// // SimpleDateFormat dateStartDate = new SimpleDateFormat("dd-MM-yyyy");
				// // Date startDate = dateStartDate.parse(startDateStr);
				// SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
				// try {
				// 	Date date = dateFormat.parse(endDateStr);
				// 	startDate = date;
				// } catch (ParseException e) {
				// 	System.out.println("Error al convertir String a Date: " + e.getMessage());
				// }
				// try {
				// 	Date date = dateFormat.parse(startDateStr);
				// 	startDate = date;
				// } catch (ParseException e) {
				// 	System.out.println("Error al convertir String a Date: " + e.getMessage());
				// }

				SendMessage messageToTelegram = new SendMessage();
				messageToTelegram.setChatId(chatId);
				messageToTelegram.setText(BotMessages.NEW_SPRINT_ADDED.getMessage());

				Sprint newSprint = new Sprint(title, status, startDate, endDate, teamID); 
				// ApplicationContext context = new AnnotationConfigApplicationContext(SprintController.class);
				// SprintController sprintController = context.getBean(SprintController.class);

				// Llamar al método createSprint del controlador para enviar el Sprint a la base de datos
				// ResponseEntity<?> responseEntity = sprintController.createSprint(newSprint);
				
				//sprintController.createSprint(newSprint);
				// ResponseEntity<?> res = sprintController.createSprint(newSprint);
				// SendMessage mess = new SendMessage();
				// mess.setChatId(chatId);
				// mess.setText(res.getBody().toString());
				// try { // Contestar si se pudo meter a la base o k
				// 	execute(mess);
				// } catch (TelegramApiException e) {
				// 	logger.error(e.getLocalizedMessage(), e);
				// }

				try { // Contestar
					execute(messageToTelegram);
				} catch (TelegramApiException e) {
					logger.error(e.getLocalizedMessage(), e);
				}
				
				userStates.put(chatId, null);

			// User (wait name)
			} else if (userStates.get(chatId).equals("WAITING_FOR_NAME")) {
				TelegramUser telegramUser = new TelegramUser();
				telegramUser.setName(messageTextFromTelegram);
				telegramUser.setAccount(chatId);
				userMap.put(chatId,telegramUser);
				promptForRole(chatId);
			
			// User (wait role)
			} else if (userStates.get(chatId).equals("WAITING_FOR_ROLE")) {
				try {
					TelegramUser temp= userMap.get(chatId);
					TelegramUser newTelegramUser = new TelegramUser(temp.getName(),chatId,messageTextFromTelegram);
					ResponseEntity entity = saveUser(newTelegramUser,chatId);		
					
					SendMessage messageToTelegram = new SendMessage();
					messageToTelegram.setChatId(chatId);
					messageToTelegram.setText("User created");
					execute(messageToTelegram);
					userStates.put(chatId, null);
					messageToTelegram = new SendMessage();
					messageToTelegram.setChatId(chatId);
					messageToTelegram.setText(BotMessages.HELLO_MYTODO_BOT.getMessage());
			
					ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
					List<KeyboardRow> keyboard = new ArrayList<>();
			
					// first row
					KeyboardRow row = new KeyboardRow();
					row.add(BotLabels.LIST_ALL_ITEMS.getLabel());
					row.add(BotLabels.ADD_NEW_ITEM.getLabel());
					// Add the first row to the keyboard
					keyboard.add(row);
			
					// second row
					row = new KeyboardRow();
					row.add(BotLabels.SHOW_MAIN_SCREEN.getLabel());
					row.add(BotLabels.HIDE_MAIN_SCREEN.getLabel());
					keyboard.add(row);
			
					// Set the keyboard
					keyboardMarkup.setKeyboard(keyboard);
			
					// Add the keyboard markup
					messageToTelegram.setReplyMarkup(keyboardMarkup);
			
					try {
						execute(messageToTelegram);
					} catch (TelegramApiException e) {
						logger.error(e.getLocalizedMessage(), e);
					}
				} catch (Exception e) {
					logger.error(e.getLocalizedMessage(), e);
				}

			}
		}
	}

	@Override
	public String getBotUsername() {		
		return botName;
	}
	//TelegramUSER
	public ResponseEntity<Boolean> findIfExists(@PathVariable("chatId") long chatId){
		Boolean flag = false;
		try {
			flag = telegramUserService.userExists(chatId);
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
}
