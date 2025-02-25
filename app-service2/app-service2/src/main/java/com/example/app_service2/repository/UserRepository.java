package com.example.app_service2.repository;

import com.example.app_service2.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Interfaccia del repository per l'entità {@link User}.
 * <p>
 * Questa interfaccia estende {@link JpaRepository} per fornire operazioni CRUD standard per l'entità {@link User}.
 * Definisce metodi di query personalizzati per accedere ai dati degli utenti in base a criteri specifici.
 * </p>
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Trova un utente in base al nome utente.
     *
     * @param username il nome utente da cercare.
     * @return un {@link Optional} contenente l'utente se trovato, altrimenti {@link Optional#empty()}.
     */
    Optional<User> findByUsername(String username);

    // Puoi definire altri metodi di query personalizzati se necessario

}