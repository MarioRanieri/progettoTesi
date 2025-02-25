package com.example.app_service2.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Rappresenta un utente nell'applicazione.
 * <p>
 * Questa classe definisce le informazioni essenziali dell'utente,
 * come l'username, la password e l'email, ed è mappata alla tabella "users" nel database.
 * </p>
 */
@Entity
@Table(name = "users")
public class User {

    /**
     * Identificatore univoco dell'utente.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nome utente univoco utilizzato per l'accesso.
     */
    @NotBlank(message = "Il nome utente è obbligatorio.")
    @Size(max = 50, message = "Il nome utente non può superare i 50 caratteri.")
    @Column(unique = true, nullable = false)
    private String username;

    /**
     * Password criptata dell'utente.
     * <p>
     * Questo campo dovrebbe contenere la password dopo l'applicazione di un algoritmo di hashing sicuro.
     * </p>
     */
    @NotBlank(message = "La password è obbligatoria.")
    @Size(min = 8, message = "La password deve contenere almeno 8 caratteri.")
    @Column(nullable = false)
    private String password;

    /**
     * Indirizzo email valido dell'utente.
     */
    @Email(message = "Inserire un indirizzo email valido.")
    @NotBlank(message = "L'email è obbligatoria.")
    @Column(nullable = false)
    private String email;

    /**
     * Versione del record per l'optimistic locking.
     */
    @Version
    @Column(name = "version")
    private Long version;

    /**
     * Timestamp della creazione dell'utente.
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp dell'ultimo aggiornamento dell'utente.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Costruttore di default richiesto da JPA.
     */
    public User() {
    }

    /**
     * Costruttore che inizializza username e password.
     *
     * @param username Nome utente.
     * @param password Password dell'utente.
     */
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Costruttore completo che inizializza tutti i campi.
     *
     * @param username Nome utente.
     * @param password Password dell'utente.
     * @param email    Indirizzo email dell'utente.
     */
    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    // Getter e Setter

    /**
     * Restituisce l'ID univoco dell'utente.
     *
     * @return ID dell'utente.
     */
    public Long getId() {
        return id;
    }

    /**
     * Imposta l'ID univoco dell'utente.
     *
     * @param id ID dell'utente.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Restituisce il nome utente.
     *
     * @return Nome utente.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Imposta il nome utente.
     *
     * @param username Nome utente.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Restituisce la password criptata dell'utente.
     *
     * @return Password criptata.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Imposta la password criptata dell'utente.
     * <p>
     * Assicurarsi che la password sia criptata utilizzando un algoritmo sicuro prima di impostarla.
     * </p>
     *
     * @param password Password criptata.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Restituisce l'indirizzo email dell'utente.
     *
     * @return Indirizzo email.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Imposta l'indirizzo email dell'utente.
     *
     * @param email Indirizzo email.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Restituisce la versione del record per l'optimistic locking.
     *
     * @return Versione del record.
     */
    public Long getVersion() {
        return version;
    }

    /**
     * Imposta la versione del record per l'optimistic locking.
     *
     * @param version Versione del record.
     */
    public void setVersion(Long version) {
        this.version = version;
    }

    /**
     * Restituisce il timestamp di creazione dell'utente.
     *
     * @return Data e ora di creazione.
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Imposta il timestamp di creazione dell'utente.
     *
     * @param createdAt Data e ora di creazione.
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Restituisce il timestamp dell'ultimo aggiornamento dell'utente.
     *
     * @return Data e ora dell'ultimo aggiornamento.
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Imposta il timestamp dell'ultimo aggiornamento dell'utente.
     *
     * @param updatedAt Data e ora dell'ultimo aggiornamento.
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Metodi equals, hashCode e toString

    /**
     * Calcola il codice hash dell'utente basato sull'ID.
     *
     * @return Codice hash calcolato.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Confronta questo utente con un altro oggetto per verificarne l'uguaglianza.
     *
     * @param obj L'oggetto da confrontare.
     * @return {@code true} se gli oggetti sono uguali, {@code false} altrimenti.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof User)) return false;
        User other = (User) obj;
        return Objects.equals(id, other.id);
    }

    /**
     * Restituisce una rappresentazione in forma di stringa dell'utente.
     *
     * @return Stringa descrittiva dell'utente.
     */
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    // Callback per impostare i timestamp

    /**
     * Metodo eseguito prima dell'inserimento dell'entità nel database.
     * Imposta il timestamp di creazione e aggiornamento.
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    /**
     * Metodo eseguito prima dell'aggiornamento dell'entità nel database.
     * Aggiorna il timestamp di aggiornamento.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
