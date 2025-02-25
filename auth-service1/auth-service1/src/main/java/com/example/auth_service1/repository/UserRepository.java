package com.example.auth_service1.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.auth_service1.model.User;

/**
 * Interfaccia del repository per l'entità {@link User}.
 * Estende {@link JpaRepository} per fornire operazioni CRUD standard su {@link User}.
 * <p>
 * Definisce metodi personalizzati per l'accesso ai dati relativi agli utenti.
 * </p>
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Trova un utente in base al nome utente.
     *
     * @param username Il nome utente da cercare.
     * @return Un {@link Optional} contenente l'utente se trovato, altrimenti vuoto.
     */
    Optional<User> findByUsername(String username);

    // Puoi aggiungere altri metodi personalizzati se necessario
}
