package com.example.app_service2.service;

import com.example.app_service2.exception.UserNotFoundException;
import com.example.app_service2.model.User;
import com.example.app_service2.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean usernameExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public User findByUsername(String username) {
        Optional<User> userOptional=userRepository.findByUsername(username);
        System.out.println("vero o ofalso? " + userOptional.isPresent());
        if (userOptional.isPresent()) {
            System.out.println("User trovato in app-service2/userService: " + userOptional.get().getUsername());
            return userOptional.get();
        } else {
            System.out.println("User non trovato in app-service2/userService: " + username);
            throw new UserNotFoundException("User non trovato in app-service2/userService con questo username: " + username);
        }
    }
    
    public boolean validateUser(String username, String password) {
        User user=findByUsername(username);
        return passwordEncoder.matches(password, user.getPassword());
    }
    
    public User saveUser(User user) {
        System.out.println("Salvataggio utente in saveUser di app-service2: " + user.getUsername() + ", " + user.getEmail());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        System.out.println("User salvato in saveUser di app-service2: " + savedUser.getUsername());
        return savedUser;
    }

    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
        System.out.println("User eliminato con ID: " + id);
    }
}




