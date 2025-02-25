package com.example.auth_service1.controller;

import com.example.auth_service1.exception.UserNotFoundException;
import com.example.auth_service1.model.User;
import com.example.auth_service1.service.UserService;
import com.example.auth_service1.util.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Collections;
import java.util.logging.Logger;

/**
 * Controller che gestisce le operazioni di autenticazione,
 * registrazione e gestione degli utenti nell'applicazione.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger LOGGER = Logger.getLogger(AuthController.class.getName());

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Endpoint per la registrazione di un nuovo utente.
     * <p>
     * Questo metodo verifica i dati di input per la registrazione di un nuovo utente.
     * Se i dati non sono validi, logga gli errori di validazione e restituisce una risposta di errore.
     * Altrimenti, procede con la registrazione dell'utente.
     * </p>
     *
     * @param user   I dati dell'utente da registrare.
     * @param result Il risultato della validazione dei dati.
     * @return La risposta HTTP con lo stato della registrazione.
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody User user, BindingResult result) {
        try {
            if (result.hasErrors()) {
                // Logga tutti gli errori di validazione
                result.getAllErrors().forEach(error -> {
                    LOGGER.warning("Errore di validazione: " + error.getDefaultMessage());
                });
                LOGGER.warning("Dati di registrazione non validi per l'utente: " + user.getUsername());
                return ResponseEntity.badRequest().body("Dati di registrazione non validi");
            }
            LOGGER.info("Tentativo di registrazione per l'utente: " + user.getUsername());
            User savedUser = userService.registerUser(user);
            LOGGER.info("Registrazione riuscita per l'utente: " + savedUser.getUsername());
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            LOGGER.severe("Errore durante la registrazione: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore durante la registrazione: " + e.getMessage());
        }
    }

    /**
     * Endpoint per l'autenticazione di un utente.
     *
     * @param username Il nome utente dell'utente.
     * @param password La password dell'utente.
     * @return La risposta HTTP con il token JWT se l'autenticazione ha successo.
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestParam String username, @RequestParam String password) {
        try {
            LOGGER.info("Tentativo di login per l'utente: " + username);
            if (userService.isUserLoggedIn(username)) {
                LOGGER.warning("Utente già loggato: " + username);
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Utente già loggato.");
            }
            User authenticatedUser = userService.authenticateUser(username, password);
            String token = jwtUtil.generateToken(authenticatedUser.getUsername());
            userService.setUserLoggedIn(username);
            LOGGER.info("Token JWT generato con successo per l'utente: " + username);
            return ResponseEntity.ok(Collections.singletonMap("token", token));
        } catch (UserNotFoundException e) {
            LOGGER.severe("Errore durante il login: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            LOGGER.severe("Errore interno durante il login: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore durante il login: " + e.getMessage());
        }
    }

    /**
     * Endpoint per ottenere le informazioni di un utente.
     *
     * @param username Il nome utente dell'utente.
     * @return La risposta HTTP con i dati dell'utente.
     */
    @GetMapping("/userinfo")
    @Cacheable(value = "userInfo", key = "#username")
    public ResponseEntity<?> getUserInfo(@RequestParam String username) {
        try {
            LOGGER.info("Richiesta di informazioni per l'utente: " + username);
            User user = userService.findByUsername(username);
            return ResponseEntity.ok(user);
        } catch (UserNotFoundException e) {
            LOGGER.warning("Utente non trovato: " + username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * Endpoint per eliminare un utente.
     *
     * @param id L'ID dell'utente da eliminare.
     * @return La risposta HTTP con lo stato dell'operazione.
     */
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser(@RequestParam Long id) {
        try {
            LOGGER.info("Tentativo di eliminazione per l'utente con ID: " + id);
            User user = userService.findById(id);
            if (user == null) {
                LOGGER.warning("ID utente non esistente: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ID utente non esistente.");
            }
            if (userService.isUserLoggedIn(user.getUsername())) {
                LOGGER.warning("Utente loggato, impossibile eliminare: " + user.getUsername());
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Utente loggato, impossibile eliminare.");
            }
            userService.deleteUserById(id);
            LOGGER.info("Utente eliminato con successo: " + user.getUsername());
            return ResponseEntity.ok(Collections.singletonMap("message", "Utente eliminato con successo"));
        } catch (UserNotFoundException e) {
            LOGGER.warning("Utente non trovato durante l'eliminazione: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ID utente non esistente.");
        } catch (Exception e) {
            LOGGER.severe("Errore interno durante l'eliminazione: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore durante l'eliminazione: " + e.getMessage());
        }
    }

    /**
     * Gestore globale delle eccezioni per il controller.
     *
     * @param e L'eccezione catturata.
     * @return La risposta HTTP con il messaggio di errore.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        LOGGER.severe("Errore interno del server: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Errore interno del server: " + e.getMessage());
    }
}
