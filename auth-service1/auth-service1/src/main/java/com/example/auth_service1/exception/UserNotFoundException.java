package com.example.auth_service1.exception;

/**
 * Eccezione personalizzata lanciata quando un utente non viene trovato nel sistema.
 * <p>
 * Questa eccezione viene tipicamente lanciata dai servizi quando un'operazione
 * richiede un utente inesistente, ad esempio durante il login o la ricerca di profili.
 * </p>
 */
public class UserNotFoundException extends RuntimeException {

    /**
     * Crea una nuova istanza di {@code UserNotFoundException} con un messaggio specifico.
     *
     * @param message Il messaggio dettagliato che descrive l'errore.
     */
    public UserNotFoundException(String message) {
        super(message);
    }
}
