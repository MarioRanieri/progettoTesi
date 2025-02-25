package com.example.app_service2.exception;

/**
 * Eccezione personalizzata lanciata quando un utente non viene trovato nel sistema.
 * <p>
 * Questa eccezione estende {@link RuntimeException} ed è tipicamente utilizzata
 * nei servizi per segnalare che un'operazione ha fallito perché l'utente specificato non esiste.
 * </p>
 */
public class UserNotFoundException extends RuntimeException {

    /**
     * Costruisce una nuova eccezione con il messaggio di dettaglio specificato.
     *
     * @param message il messaggio di dettaglio che descrive l'errore.
     */
    public UserNotFoundException(String message) {
        super(message);
    }

    /**
     * Costruisce una nuova eccezione con il messaggio di dettaglio e la causa specificati.
     *
     * @param message il messaggio di dettaglio che descrive l'errore.
     * @param cause   la causa (un'eccezione che ha provocato questa eccezione).
     */
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}