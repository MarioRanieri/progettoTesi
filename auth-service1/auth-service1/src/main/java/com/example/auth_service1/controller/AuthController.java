package com.example.auth_service1.controller;

import com.example.auth_service1.exception.UserNotFoundException;
import com.example.auth_service1.model.User;
import com.example.auth_service1.service.UserService;
import com.example.auth_service1.util.JwtUtil;
import java.util.Collections;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger LOGGER = Logger.getLogger(AuthController.class.getName());

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            LOGGER.info("Tentativo di registrazione per l'utente in auth-serive1/AuthController: " + user.getUsername());
            User savedUser = userService.registerUser(user);
            LOGGER.info("Registrazione riuscita per l'utente in auth-serive1/AuthController: " + savedUser.getUsername());
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            LOGGER.severe("Errore durante la registrazione in auth-serive1/AuthController: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Errore durante la registrazione in auth-serive1/AuthController: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestParam String username, @RequestParam String password) {
        try {
            LOGGER.info("in auth-serive1/AuthController, Tentativo di login per l'utente: " + username);
            User authenticatedUser = userService.authenticateUser(username, password);
            String token = jwtUtil.generateToken(authenticatedUser.getUsername(), authenticatedUser.getAuthorities());
            LOGGER.info("in auth-serive1/AuthController, Token JWT generato con successo per l'utente: " + username);
            return ResponseEntity.ok(Collections.singletonMap("token", token));
        } catch (UserNotFoundException e) {
            LOGGER.severe("in auth-serive1/AuthController, Errore durante il login: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/userinfo")
    public ResponseEntity<?> getUserInfo(@RequestParam String username) {
        try {
            User user = userService.findByUsername(username);
            return ResponseEntity.ok(user);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser(@RequestParam Long id) {
        userService.deleteUserById(id);
        return ResponseEntity.ok(Collections.singletonMap("message", "User deleted successfully"));
    }
}




