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
import java.util.Set;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        System.out.println("User salvato in Auth-service1/UserService: " + savedUser.getUsername());
        return savedUser;
    }

    public User findByUsername(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            System.out.println("User trovato: " + userOptional.get().getUsername());
            return userOptional.get();
        } else {
            System.out.println("User non trovato: " + username);
            throw new UserNotFoundException("User not found with username: " + username);
        }
    }

    public boolean validatePassword(String rawPassword, String encodedPassword) {
        boolean isValid = passwordEncoder.matches(rawPassword, encodedPassword);
        System.out.println("Password valida: " + isValid);
        return isValid;
    }

    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
        System.out.println("User eliminato con ID: " + id);
    }

    // Metodo per la registrazione con chiamata a app-service2
    public User registerUser(User user) {
        // Verifica se l'username esiste già in app-service2
        HttpResponse<JsonNode> response = Unirest.get("http://localhost:8082/app-service2/check-username")
                .queryString("username", user.getUsername())
                .asJson();
        System.out.println("Controllo dell'username in app-service2 effettuato! questa è la chiamata unirest in auth-service1/UserService ");
        if (response.getBody().getObject().getBoolean("exists")) {
            throw new IllegalArgumentException("Username già in uso!");
        }
        // Assegna un ruolo all'utente 
        if (user.getAuthorities() == null || user.getAuthorities().isEmpty()) { 
            user.setAuthorities(Set.of("ROLE_USER")); // Assegna il ruolo di default 
            }

        // Salva l'utente in auth-service1
        User savedUser = this.saveUser(user);
        System.out.println("User registrato: " + savedUser.getUsername());
        // Sincronizza l'utente con app-service2 senza salvarlo di nuovo
        response = Unirest.post("http://localhost:8082/app-service2/register")
                .header("Content-Type", "application/json")
                .body(savedUser)
                .asJson();
        System.out.println("Stato della risposta: " + response.getStatus());
        System.out.println("Corpo della risposta: " + response.getBody().toString());
        if (response.getStatus() != 201) {
            throw new RuntimeException("Errore nella sincronizzazione con app-service2: " + response.getBody());
        }
        return savedUser;
    }
    
    // Metodo per l'autenticazione con chiamata a app-service2
    public User authenticateUser(String username, String password) throws UserNotFoundException {
        System.out.println("Autenticazione utente in auth-service1/userService/authenticateUser: " + username);
        // Crea un nuovo utente con i dati forniti 
        User tempUser = new User(); 
        tempUser.setUsername(username); 
        tempUser.setPassword(password); 
        //test di stampa
        System.out.println("nome: " + tempUser.getUsername());
        
        // Passa l'oggetto utente completo nella richiesta a app-service2
        HttpResponse<JsonNode> response = Unirest.post("http://localhost:8082/app-service2/validate-user")
                .header("Content-Type", "application/json")
                .body(tempUser)
                .asJson();
        System.out.println("Stato della risposta: " + response.getStatus());
        System.out.println("Corpo della risposta: " + response.getBody().toString());
        if (response.getStatus() != 200) {
            System.out.println("Autenticazione fallita in auth-service1/userService/authenticateUser per: " + username);
            throw new UserNotFoundException("in auth-service1/UserService, Invalid username or password");
        }
        User user = this.findByUsername(username);
        if (this.validatePassword(password, user.getPassword())) {
            System.out.println("User autenticato in auth-service1/UserService: " + username);
            return user;
        } else {
            System.out.println("Password non valida per: " + username);
            throw new UserNotFoundException("in auth-service1/UserService, Invalid username or password");
        }
    }

    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }
}







