package com.example.app_service2.service;

import com.example.app_service2.exception.UserNotFoundException;
import com.example.app_service2.model.User;
import com.example.app_service2.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.logging.Logger;

/**
 * Servizio che gestisce le operazioni relative agli utenti nell'applicazione.
 * <p>
 * Fornisce metodi per la verifica dell'esistenza di un nome utente,
 * la ricerca di un utente, la validazione delle credenziali,
 * il salvataggio di un nuovo utente e la cancellazione di un utente.
 * </p>
 */
@Service
public class UserService {

    private static final Logger LOGGER = Logger.getLogger(UserService.class.getName());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Verifica se un nome utente esiste già nel sistema.
     *
     * @param username il nome utente da verificare.
     * @return {@code true} se il nome utente esiste, {@code false} altrimenti.
     */
    public boolean usernameExists(String username) {
        boolean exists = userRepository.findByUsername(username).isPresent();
        LOGGER.info("Verifica dell'esistenza del nome utente '" + username + "': " + exists);
        return exists;
    }

    /**
     * Trova un utente in base al nome utente.
     *
     * @param username il nome utente dell'utente da trovare.
     * @return l'utente trovato.
     * @throws UserNotFoundException se l'utente non viene trovato.
     */
    public User findByUsername(String username) throws UserNotFoundException {
        Optional<User> userOptional = userRepository.findByUsername(username);
        LOGGER.info("Ricerca dell'utente con nome utente: " + username);
        if (userOptional.isPresent()) {
            LOGGER.info("Utente trovato: " + userOptional.get().getUsername());
            return userOptional.get();
        } else {
            LOGGER.warning("Utente non trovato: " + username);
            throw new UserNotFoundException("Utente non trovato con questo nome utente: " + username);
        }
    }

    /**
     * Valida le credenziali di un utente confrontando la password fornita con quella memorizzata.
     *
     * @param username il nome utente dell'utente.
     * @param password la password fornita dall'utente.
     * @return {@code true} se le credenziali sono valide, {@code false} altrimenti.
     * @throws UserNotFoundException se l'utente non viene trovato.
     */
    public boolean validateUser(String username, String password) throws UserNotFoundException {
        User user = findByUsername(username);
        boolean isValid = passwordEncoder.matches(password, user.getPassword());
        LOGGER.info("Validazione delle credenziali per l'utente '" + username + "': " + isValid);
        return isValid;
    }

    /**
     * Salva un nuovo utente nel database dopo aver criptato la password.
     *
     * @param user l'utente da salvare.
     * @return l'utente salvato con l'ID generato.
     */
    public User saveUser(User user) {
        LOGGER.info("Salvataggio dell'utente: " + user.getUsername() + ", " + user.getEmail());

        // Cripta la password prima di salvarla
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(user);
        LOGGER.info("Utente salvato con successo: " + savedUser.getUsername());
        return savedUser;
    }

    /**
     * Elimina un utente dal database in base all'ID.
     *
     * @param id l'ID dell'utente da eliminare.
     */
    public void deleteUserById(Long id) {
        LOGGER.info("Eliminazione dell'utente con ID: " + id);
        userRepository.deleteById(id);
        LOGGER.info("Utente eliminato con successo con ID: " + id);
    }
}





