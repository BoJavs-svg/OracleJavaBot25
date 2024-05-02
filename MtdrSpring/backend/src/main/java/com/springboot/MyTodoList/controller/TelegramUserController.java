// package com.springboot.MyTodoList.controller;

// import com.springboot.MyTodoList.model.TelegramUser;
// import com.springboot.MyTodoList.service.TelegramUserService;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpHeaders;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.util.List;
// import java.util.Optional;

// @RestController
// @RequestMapping("/api/telegramusers") // Base path for the controller
// public class TelegramUserController {

//     @Autowired
//     private TelegramUserService telegramUserService;

//     @GetMapping
//     public ResponseEntity<String> checkTelegramUserTableExists() {
//         return ResponseEntity.ok(telegramUserService.checkTelegramUserTableExists());
//     }

//     @PostMapping
//     public ResponseEntity<TelegramUser> saveTelegramUser(@RequestBody TelegramUser telegramUser) {
//         try {
//             TelegramUser savedUser = telegramUserService.saveTelegramUser(telegramUser);
//             if (savedUser!= null) {
//                 return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
//             } else {
//                 return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
//             }
//         } catch (Exception e) {
//             return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//         }
//     }
    
//     @GetMapping("/{accountID}")
//     public ResponseEntity<Boolean> userExistsByAccount(@PathVariable Long accountID) {
//         return ResponseEntity.ok(telegramUserService.userExists(accountID));
//     }

//     @GetMapping("/{id}")
//     public ResponseEntity<TelegramUser> getTelegramUserById(@PathVariable Long id) {
//         try {
//             Optional<TelegramUser> optionalUser = telegramUserService.findById(id);
//             if (optionalUser.isPresent()) {
//                 return new ResponseEntity<>(optionalUser.get(), HttpStatus.OK);
//             } else {
//                 return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//             }
//         } catch (Exception e) {
//             return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//         }
//     }

//     @PutMapping("/{id}")
//     public ResponseEntity<TelegramUser> updateTelegramUser(@PathVariable Long id, @RequestBody TelegramUser updatedUser) {
//         try {
//             TelegramUser updatedUserOptional = telegramUserService.updateTelegramUser(id, updatedUser);
//             return new ResponseEntity<>(updatedUserOptional, HttpStatus.OK);
//         } catch (Exception e) {
//             return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//         }
//     }
// }