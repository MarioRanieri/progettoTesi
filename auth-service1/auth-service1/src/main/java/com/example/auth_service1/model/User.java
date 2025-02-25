package com.example.auth_service1.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Rappresenta un utente nel sistema di autenticazione.
 * Contiene le informazioni essenziali per l'autenticazione e l'identificazione dell'utente.
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
     * Questo campo è ignorato durante la serializzazione JSON per motivi di sicurezza.
     */
    @NotBlank(message = "La password è obbligatoria.")
    @Size(min = 8, message = "La password deve contenere almeno 8 caratteri.")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password;

    /**
     * Indirizzo email dell'utente.
     */
    @Email(message = "Inserire un indirizzo email valido.")
    @NotBlank(message = "L'email è obbligatoria.")
    @Size(max = 100, message = "L'email non può superare i 100 caratteri.")
    @Column(nullable = false)
    private String email;

    /**
     * Versione del record per la gestione dell'optimistic locking.
     */
    @Version
    @Column(name = "version")
    private Long version;

    /**
     * Costruttore di default necessario per JPA.
     */
    public User() {
    }

    /**
     * Costruttore che inizializza username e password.
     *
     * @param username Nome utente dell'account.
     * @param password Password dell'account.
     */
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Costruttore che inizializza tutti i campi.
     *
     * @param username Nome utente dell'account.
     * @param password Password dell'account.
     * @param email    Indirizzo email dell'utente.
     */
    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    // Getter e Setter

    /**
     * Restituisce l'ID dell'utente.
     *
     * @return ID univoco dell'utente.
     */
    public Long getId() {
        return id;
    }

    /**
     * Imposta l'ID dell'utente.
     *
     * @param id ID univoco dell'utente.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Restituisce il nome utente.
     *
     * @return Nome utente dell'account.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Imposta il nome utente.
     *
     * @param username Nome utente dell'account.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Restituisce la password criptata.
     *
     * @return Password criptata dell'account.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Imposta la password criptata.
     *
     * **Nota**: Assicurati di criptare la password prima di chiamare questo metodo.
     *
     * @param password Password criptata dell'account.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Restituisce l'indirizzo email dell'utente.
     *
     * @return Indirizzo email dell'utente.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Imposta l'indirizzo email dell'utente.
     *
     * @param email Indirizzo email dell'utente.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Restituisce la versione del record.
     *
     * @return Versione del record per l'optimistic locking.
     */
    public Long getVersion() {
        return version;
    }

    /**
     * Imposta la versione del record.
     *
     * @param version Versione del record per l'optimistic locking.
     */
    public void setVersion(Long version) {
        this.version = version;
    }

    // Metodi override

    /**
     * Genera un codice hash basato sull'ID dell'utente.
     *
     * @return Il codice hash dell'utente.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Confronta questo utente con un altro oggetto.
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
     * Restituisce una rappresentazione in stringa dell'utente.
     *
     * @return Stringa che rappresenta l'utente.
     */
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}