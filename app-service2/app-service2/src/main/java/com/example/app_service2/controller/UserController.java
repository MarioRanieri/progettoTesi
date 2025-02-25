package com.example.app_service2.controller;

import com.example.app_service2.model.User;
import com.example.app_service2.service.UserService;
import com.example.app_service2.util.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Controller che gestisce le operazioni relative agli utenti nell'applicazione.
 * <p>
 * Fornisce endpoint per controllare l'esistenza di un nome utente,
 * validare le credenziali di un utente, registrare un nuovo utente
 * e accedere a un endpoint protetto per gli utenti autenticati.
 * </p>
 */
@RestController
@RequestMapping("/app-service2")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    private static final Logger LOGGER = Logger.getLogger(UserController.class.getName());

    /**
     * Endpoint per verificare se un nome utente esiste già nel sistema.
     *
     * @param username il nome utente da verificare.
     * @return una risposta che indica se il nome utente esiste.
     */
    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsername(@RequestParam String username) {
        boolean exists = userService.usernameExists(username);
        LOGGER.info("Verifica dell'esistenza del nome utente: " + username + " - Esiste: " + exists);
        return ResponseEntity.ok(Collections.singletonMap("exists", exists));
    }

    /**
     * Endpoint per validare le credenziali di un utente.
     *
     * @param credentials una mappa contenente "username" e "password".
     * @return una risposta che indica se le credenziali sono valide.
     */
    @PostMapping("/validate-user")
    public ResponseEntity<?> validateUser(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");
        LOGGER.info("Validazione utente: " + username);
        boolean isValid = userService.validateUser(username, password);
        if (isValid) {
            LOGGER.info("Utente validato con successo: " + username);
            return ResponseEntity.ok(Collections.singletonMap("message", "User is valid"));
        } else {
            LOGGER.warning("Credenziali non valide per l'utente: " + username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("message", "Invalid username or password"));
        }
    }

    /**
     * Endpoint per registrare un nuovo utente.
     *
     * @param user l'utente da registrare.
     * @return la risposta con lo stato della registrazione.
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        LOGGER.info("Registrazione dell'utente: " + user.getUsername() + ", " + user.getEmail());
        try {
            // Sincronizza l'utente senza salvarlo di nuovo
            LOGGER.info("Utente ricevuto da auth-service1: " + user.getUsername());
            // Non salviamo nuovamente l'utente per evitare duplicazioni
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (Exception e) {
            LOGGER.severe("Errore durante la registrazione: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Errore durante la registrazione"));
        }
    }

    /**
     * Endpoint protetto per gli utenti autenticati.
     * <p>
     * Verifica il token JWT fornito nell'header Authorization e consente l'accesso
     * solo se il token è valido.
     * </p>
     *
     * @param headers gli header HTTP della richiesta.
     * @return un messaggio che indica l'esito dell'accesso.
     */
    @GetMapping("/user-endpoint")
    public ResponseEntity<String> userEndpoint(@RequestHeader HttpHeaders headers) {
        LOGGER.info("Accesso all'endpoint protetto /user-endpoint");
        String authorizationHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            LOGGER.warning("Header Authorization mancante o non contiene un token Bearer");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Authorization header is missing or invalid");
        }

        String token = authorizationHeader.substring(7);
        LOGGER.info("Token ricevuto: " + token);

        try {
            // Estrai il nome utente dal token
            String username = jwtUtil.getClaims(token).getSubject();
            User user = userService.findByUsername(username);

            // Valida il token
            if (!jwtUtil.validateToken(token, user)) {
                LOGGER.warning("Token JWT non valido per l'utente: " + username);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid JWT token");
            }

            LOGGER.info("Accesso consentito all'utente: " + username);
            return ResponseEntity.ok("Accesso riuscito per l'utente: " + username);

        } catch (Exception e) {
            LOGGER.severe("Errore durante la validazione del token: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore durante la validazione del token");
        }
    }
}