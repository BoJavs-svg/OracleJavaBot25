package com.springboot.MyTodoList.controller;

import static org.mockito.ArgumentMatchers.booleanThat;
import static org.mockito.ArgumentMatchers.eq;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

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
	
	public ToDoItemBotController(String botToken, String botName, ToDoItemService toDoItemService) {
		super(botToken);
		logger.info("Bot Token: " + botToken);
		logger.info("Bot name: " + botName);
		this.toDoItemService = toDoItemService;
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
		}else if (userStates.get(chatId).equals("WAITING_FOR_NAME")) {
			TelegramUser telegramUser = new TelegramUser();
			telegramUser.setName(messageTextFromTelegram);
			telegramUser.setAccount(chatId);
			userMap.put(chatId,telegramUser);
			promptForRole(chatId);

		} else if (userStates.get(chatId).equals("WAITING_FOR_ROLE")) {
			try {
				TelegramUser telegramUser = userMap.get(chatId);
				telegramUser.setRol(messageTextFromTelegram);
				ResponseEntity entity = saveUser(telegramUser,chatId);		
				
				
				SendMessage messageToTelegram = new SendMessage();
				messageToTelegram.setChatId(chatId);
				messageToTelegram.setText(entity.getBody().toString());
				execute(messageToTelegram);
				userStates.put(chatId, null);
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
			return new ResponseEntity<>(flag, HttpStatus.NOT_FOUND);
		}

	}
	public ResponseEntity saveUser(@RequestBody TelegramUser telegramUser, long chatId) {
		try {
			TelegramUser tu = telegramUserService.saveTelegramUser(telegramUser);
			SendMessage messageToTelegram = new SendMessage();
			messageToTelegram.setChatId(chatId);
			messageToTelegram.setText("telegramUser: " + tu.toString());
			execute(messageToTelegram);
			HttpHeaders responseHeaders = new HttpHeaders();
			responseHeaders.set("location", "" + tu.getId());
			responseHeaders.set("Access-Control-Expose-Headers", "location");
			return ResponseEntity.ok().headers(responseHeaders).build();
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving user: " + e.getMessage());
		}
	}
	
	//Prompts
	public void promptForUserInformation(long chatId) {
		SendMessage message = new SendMessage();
		message.setChatId(chatId);
		message.setText("Please enter your name:");
		try {
			execute(message);
		} catch (TelegramApiException e) {
			logger.error(e.getLocalizedMessage(), e);
		}	
		userStates.put(chatId, "WAITING_FOR_NAME");
	}
	
	public void promptForRole(long chatId) {
		SendMessage message = new SendMessage();
		message.setChatId(chatId);
		message.setText("Please select your role:");
	
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
