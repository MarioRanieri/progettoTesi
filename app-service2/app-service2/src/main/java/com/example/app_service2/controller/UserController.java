package com.example.app_service2.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.app_service2.service.UserService;
import com.example.app_service2.util.JwtUtil;
import com.example.app_service2.model.User;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/app-service2")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    private static final Logger LOGGER = Logger.getLogger(UserController.class.getName());

    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsername(@RequestParam String username) {
        boolean exists = userService.usernameExists(username);
        return ResponseEntity.ok(Collections.singletonMap("exists", exists));
    }

    @PostMapping("/validate-user")
    public ResponseEntity<?> validateUser(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");
        boolean isValid = userService.validateUser(username, password);
        System.out.println("user validato in /userController/validate-user: " + credentials);
        if (isValid) {
            return ResponseEntity.ok(Collections.singletonMap("message", "User is valid"));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("message", "Invalid username or password"));
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        System.out.println("Dati ricevuti per registrazione in app-service2/UserController: " + user.getUsername() + ", " + user.getEmail());
        try {
            // Sincronizza l'utente senza salvarlo di nuovo 
            System.out.println("Utente ricevuto da auth-service1: " + user.getUsername()); 
            return ResponseEntity.status(201).body(user);
        } catch (Exception e) {
            System.out.println("Errore durante la registrazione in app-service2/UserController: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("message", "Errore durante la registrazione in app-service2/UserController"));
        }
    }

    @GetMapping("/user-endpoint") 
    public String userEndpoint(@RequestHeader HttpHeaders headers) { 
        String authorizationHeader = headers.getFirst(HttpHeaders.AUTHORIZATION); 
        
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
             LOGGER.severe("Authorization header is missing or does not contain a Bearer token"); 
             return "Authorization header is missing or invalid"; 
            } 
            String token = authorizationHeader.substring(7); LOGGER.info("Token ricevuto: " + token); 
            if (!jwtUtil.validateToken(token)) { 
                LOGGER.severe("Token JWT non valido"); 
                return "Invalid JWT token"; 
            } 
            return "Accesso riuscito"; 
        }
}







