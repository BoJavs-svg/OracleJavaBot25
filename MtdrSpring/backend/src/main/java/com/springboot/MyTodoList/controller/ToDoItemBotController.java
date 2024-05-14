package com.springboot.MyTodoList.controller;

import static org.mockito.ArgumentMatchers.booleanThat;
import static org.mockito.ArgumentMatchers.eq;

import java.time.OffsetDateTime;
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

import com.springboot.MyTodoList.service.ToDoItemService;
import com.springboot.MyTodoList.service.TelegramUserService;


import com.springboot.MyTodoList.util.BotCommands;
import com.springboot.MyTodoList.util.BotHelper;
import com.springboot.MyTodoList.util.BotLabels;
import com.springboot.MyTodoList.util.BotMessages;

import java.util.Map;
import java.util.HashMap;


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
			logger.info("Received message ("+chatId+"): " + messageTextFromTelegram);
			SendMessage message = new SendMessage();
			message.setChatId(chatId);
			message.setText("Mensaje recibido " + messageTextFromTelegram);
			try{
				execute(message);
			}catch(TelegramApiException e){
				logger.error("Error en mensaje recibido");
			}
			if (messageTextFromTelegram.equals(BotCommands.START_COMMAND.getCommand())
					|| messageTextFromTelegram.equals(BotLabels.SHOW_MAIN_SCREEN.getLabel())) {
				ResponseEntity<Boolean> response = findIfExists(chatId);
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
					SendMessage messageToTelegram = new SendMessage();
					messageToTelegram.setChatId(chatId);
					messageToTelegram.setText(BotMessages.HELLO_MYTODO_BOT.getMessage());
					
				}
		}else if (messageTextFromTelegram.equals(BotCommands.ADD_TASK.getCommand())
				|| messageTextFromTelegram.equals(BotLabels.ADD_NEW_TASK.getLabel())){
		
		}else if (userStates.get(chatId).equals("WAITING_FOR_NAME")) {
			TelegramUser telegramUser = new TelegramUser();
			telegramUser.setName(messageTextFromTelegram);
			telegramUser.setAccount(chatId);
			userMap.put(chatId,telegramUser);
			promptForRole(chatId);

		} else if (userStates.get(chatId).equals("WAITING_FOR_ROLE")) {
			try {
				TelegramUser temp= userMap.get(chatId);
				TelegramUser newTelegramUser = new TelegramUser(temp.getName(),chatId,messageTextFromTelegram);
				ResponseEntity entity = saveUser(newTelegramUser,chatId);		
				
				SendMessage messageToTelegram = new SendMessage();
				messageToTelegram.setChatId(chatId);
				messageToTelegram.setText("User created");
				execute(messageToTelegram);
				userMap.put(chatId,null);
				userStates.put(chatId, null);
				messageToTelegram = new SendMessage();
				messageToTelegram.setChatId(chatId);
				messageToTelegram.setText(BotMessages.HELLO_MYTODO_BOT.getMessage());
				markupKB(chatId);
				execute(messageToTelegram);				
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
	//Markup keyboard
	public void markupKB(long chatId) {
		ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
		List<KeyboardRow> keyboard = new ArrayList<>();
		KeyboardRow row = new KeyboardRow();
		row.add(BotLabels.SHOW_MAIN_SCREEN.getLabel());
		Optional<TelegramUser> userOpt = telegramUserService.getUserbyAccount(chatId);
		if (userOpt.isPresent()) {
			TelegramUser user = userOpt.get();
			if ("Manager".equals(user.getRol())) {
				row.add(BotLabels.ADD_NEW_TASK.getLabel());
			} else if ("Developer".equals(user.getRol())) {
				// Add options for Developer
			}
		}
		keyboard.add(row);

		keyboardMarkup.setKeyboard(keyboard);
		SendMessage messageToTelegram = new SendMessage();

		messageToTelegram.setReplyMarkup(keyboardMarkup);

		try {
			execute(messageToTelegram);
		} catch (TelegramApiException e) {
			logger.error(e.getLocalizedMessage(), e);
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
