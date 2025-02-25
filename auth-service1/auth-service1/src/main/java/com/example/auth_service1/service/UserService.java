package com.example.auth_service1.service;

import com.example.auth_service1.exception.UserNotFoundException;
import com.example.auth_service1.model.User;
import com.example.auth_service1.repository.UserRepository;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servizio che gestisce le operazioni relative agli utenti,
 * tra cui registrazione, autenticazione e comunicazione con app-service2.
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Mappa thread-safe per tenere traccia degli utenti loggati.
     */
    private final ConcurrentHashMap<String, Boolean> loggedInUsers = new ConcurrentHashMap<>();

    /**
     * Salva un nuovo utente nel database locale dopo aver criptato la password.
     *
     * @param user L'utente da salvare.
     * @return L'utente salvato con ID generato.
     */
    public User saveUser(User user) {
        // Cripta la password prima di salvarla
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        System.out.println("Utente salvato in Auth-service1/UserService: " + savedUser.getUsername());
        return savedUser;
    }

    /**
     * Trova un utente nel database locale tramite ID.
     *
     * @param id L'ID dell'utente da cercare.
     * @return L'utente trovato o {@code null} se non esiste.
     */
    public User findById(Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        return userOptional.orElse(null);
    }

    /**
     * Trova un utente nel database locale tramite username.
     *
     * @param username Il nome utente da cercare.
     * @return L'utente trovato.
     * @throws UserNotFoundException Se l'utente non viene trovato.
     */
    public User findByUsername(String username) throws UserNotFoundException {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            System.out.println("Utente trovato: " + userOptional.get().getUsername());
            return userOptional.get();
        } else {
            System.out.println("Utente non trovato: " + username);
            throw new UserNotFoundException("Utente non trovato con username: " + username);
        }
    }

    /**
     * Valida la password inserita confrontandola con quella criptata memorizzata.
     *
     * @param rawPassword     La password in chiaro inserita dall'utente.
     * @param encodedPassword La password criptata memorizzata nel database.
     * @return {@code true} se le password corrispondono, {@code false} altrimenti.
     */
    public boolean validatePassword(String rawPassword, String encodedPassword) {
        boolean isValid = passwordEncoder.matches(rawPassword, encodedPassword);
        System.out.println("Password valida: " + isValid);
        return isValid;
    }

    /**
     * Elimina un utente dal database locale tramite ID.
     *
     * @param id L'ID dell'utente da eliminare.
     */
    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
        System.out.println("Utente eliminato con ID: " + id);
    }

    /**
     * Registra un nuovo utente effettuando una chiamata a app-service2 per verificare l'unicità
     * dell'username e sincronizza l'utente con app-service2.
     *
     * @param user L'utente da registrare.
     * @return L'utente registrato.
     * @throws IllegalArgumentException Se l'username è già in uso in app-service2.
     * @throws RuntimeException         Se si verifica un errore durante la sincronizzazione con app-service2.
     */
    public User registerUser(User user) {
        // Verifica se l'username esiste già in app-service2
        HttpResponse<JsonNode> response = Unirest.get("http://localhost:8082/app-service2/check-username")
                .queryString("username", user.getUsername())
                .asJson();

        System.out.println("Controllo dell'username in app-service2 effettuato!");

        if (response.getBody().getObject().getBoolean("exists")) {
            throw new IllegalArgumentException("Username già in uso!");
        }

        // Salva l'utente in auth-service1
        User savedUser = this.saveUser(user);
        System.out.println("Utente registrato: " + savedUser.getUsername());

        // Sincronizza l'utente con app-service2 senza salvare nuovamente la password criptata
        User userToSync = new User();
        userToSync.setUsername(savedUser.getUsername());
        userToSync.setEmail(savedUser.getEmail());
        // Invia la password in chiaro, assicurandoti che sia gestita in modo sicuro
        userToSync.setPassword(user.getPassword());

        response = Unirest.post("http://localhost:8082/app-service2/register")
                .header("Content-Type", "application/json")
                .body(userToSync)
                .asJson();

        System.out.println("Stato della risposta: " + response.getStatus());
        System.out.println("Corpo della risposta: " + response.getBody().toString());

        if (response.getStatus() != 201) {
            throw new RuntimeException("Errore nella sincronizzazione con app-service2: " + response.getBody());
        }

        return savedUser;
    }

    /**
     * Autentica un utente verificando le credenziali e comunica con app-service2 per la validazione.
     *
     * @param username Il nome utente.
     * @param password La password in chiaro inserita dall'utente.
     * @return L'utente autenticato.
     * @throws UserNotFoundException Se l'autenticazione fallisce.
     */
    public User authenticateUser(String username, String password) throws UserNotFoundException {
        System.out.println("Autenticazione utente: " + username);

        // Crea un oggetto User temporaneo con i dati forniti
        User tempUser = new User();
        tempUser.setUsername(username);
        tempUser.setPassword(password);

        // Passa l'oggetto utente completo nella richiesta a app-service2
        HttpResponse<JsonNode> response = Unirest.post("http://localhost:8082/app-service2/validate-user")
                .header("Content-Type", "application/json")
                .body(tempUser)
                .asJson();

        System.out.println("Stato della risposta: " + response.getStatus());
        System.out.println("Corpo della risposta: " + response.getBody().toString());

        if (response.getStatus() != 200) {
            System.out.println("Autenticazione fallita per: " + username);
            throw new UserNotFoundException("Username o password non validi");
        }

        User user = this.findByUsername(username);
        if (this.validatePassword(password, user.getPassword())) {
            System.out.println("Utente autenticato: " + username);
            return user;
        } else {
            System.out.println("Password non valida per: " + username);
            throw new UserNotFoundException("Username o password non validi");
        }
    }

    /**
     * Restituisce l'istanza di PasswordEncoder utilizzata per criptare le password.
     *
     * @return L'istanza di PasswordEncoder.
     */
    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }

    // Gestione dello stato di login

    /**
     * Verifica se un utente è attualmente loggato.
     *
     * @param username Il nome utente da verificare.
     * @return {@code true} se l'utente è loggato, {@code false} altrimenti.
     */
    public boolean isUserLoggedIn(String username) {
        return loggedInUsers.getOrDefault(username, false);
    }

    /**
     * Imposta lo stato di un utente come loggato.
     *
     * @param username Il nome utente da impostare come loggato.
     */
    public void setUserLoggedIn(String username) {
        loggedInUsers.put(username, true);
    }

    /**
     * Imposta lo stato di un utente come non loggato.
     *
     * @param username Il nome utente da impostare come non loggato.
     */
    public void setUserLoggedOut(String username) {
        loggedInUsers.remove(username);
    }
}
