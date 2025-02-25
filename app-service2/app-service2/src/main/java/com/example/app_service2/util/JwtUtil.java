package com.example.app_service2.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import com.example.app_service2.model.User;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;

import java.util.Base64;
import java.util.Date;
import java.util.Set;
import java.util.HashSet;
import java.util.logging.Logger;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigInteger;
import java.security.spec.RSAPublicKeySpec;
import java.util.List;
import java.util.Map;

/**
 * Utilità per la gestione dei token JWT nell'applicazione.
 * <p>
 * Questa classe fornisce metodi per validare i token JWT, estrarre le claim e invalidare i token.
 * Si basa sulla chiave pubblica fornita da auth-service1 per la verifica dei token.
 * </p>
 */
@Component
public class JwtUtil {

    private static final Logger LOGGER = Logger.getLogger(JwtUtil.class.getName());

    private static final String JWKS_URI = "http://localhost:8081/oauth2/jwks";

    /**
     * Insieme dei token invalidati, utilizzato per tracciare i token che sono stati esplicitamente revocati.
     */
    private final Set<String> invalidatedTokens = new HashSet<>();

    /**
     * Chiave pubblica utilizzata per la verifica dei token JWT.
     */
    private RSAPublicKey publicKey;

    /**
     * Costruttore della classe JwtUtil.
     * <p>
     * Recupera la chiave pubblica dal JWKS fornito da auth-service1.
     * </p>
     *
     * @throws RuntimeException se si verifica un errore durante il recupero della chiave pubblica.
     */
    public JwtUtil() {
        try {
            LOGGER.info("Inizio il recupero della chiave pubblica dal JWKS");
            Map<String, Object> jwks = fetchJwks();
            this.publicKey = getPublicKeyFromJwks(jwks);
            LOGGER.info("Chiave pubblica recuperata con successo");
        } catch (Exception e) {
            LOGGER.severe("Errore durante il recupero della chiave pubblica: " + e.getMessage());
            throw new RuntimeException("Errore durante il recupero della chiave pubblica", e);
        }
    }

    /**
     * Estrae le claim contenute in un token JWT.
     *
     * @param token il token JWT da analizzare.
     * @return le claim estratte dal token.
     * @throws RuntimeException se il token non è valido.
     */
    public Claims getClaims(String token) {
        LOGGER.info("Estrazione delle claim dal token JWT");
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            LOGGER.severe("Errore durante l'analisi del token JWT: " + e.getMessage());
            throw new RuntimeException("Token JWT non valido", e);
        }
    }

    /**
     * Valida un token JWT verificando la firma, la scadenza e l'appartenenza all'utente specificato.
     *
     * @param token il token JWT da validare.
     * @param user  l'utente associato al token.
     * @return {@code true} se il token è valido e appartiene all'utente; {@code false} altrimenti.
     */
    public boolean validateToken(String token, User user) {
        LOGGER.info("Validazione del token JWT");
        try {
            Claims claims = getClaims(token);
            String username = claims.getSubject();

            // Controlla se il token è scaduto
            if (isTokenExpired(claims)) {
                LOGGER.warning("Token JWT scaduto");
                return false;
            }

            // Controlla se il token è stato invalidato
            if (isTokenInvalidated(token)) {
                LOGGER.warning("Token JWT invalidato");
                return false;
            }

            // Verifica che il token appartenga all'utente corretto
            if (!username.equals(user.getUsername())) {
                LOGGER.warning("Il token JWT non appartiene all'utente specificato");
                return false;
            }

            return true;
        } catch (Exception e) {
            LOGGER.severe("Validazione del token JWT fallita: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verifica se un token JWT è stato invalidato.
     *
     * @param token il token JWT da verificare.
     * @return {@code true} se il token è stato invalidato; {@code false} altrimenti.
     */
    private boolean isTokenInvalidated(String token) {
        return invalidatedTokens.contains(token);
    }

    /**
     * Invalida un token JWT, impedendone l'uso futuro.
     *
     * @param token il token JWT da invalidare.
     */
    public void invalidateToken(String token) {
        invalidatedTokens.add(token);
        LOGGER.info("Token JWT invalidato con successo");
    }

    /**
     * Verifica se un token JWT è scaduto.
     *
     * @param claims le claim estratte dal token JWT.
     * @return {@code true} se il token è scaduto; {@code false} altrimenti.
     */
    private boolean isTokenExpired(Claims claims) {
        boolean expired = claims.getExpiration().before(new Date());
        LOGGER.info("Token JWT scaduto: " + expired);
        return expired;
    }

    /**
     * Recupera il JWKS dall'URI specificato.
     *
     * @return una mappa contenente il JWKS.
     * @throws Exception se si verifica un errore durante il recupero o la lettura del JWKS.
     */
    private Map<String, Object> fetchJwks() throws Exception {
        HttpResponse<String> response = Unirest.get(JWKS_URI).asString();
        if (response.getStatus() != 200) {
            throw new RuntimeException("Errore nel recupero del JWKS: status " + response.getStatus());
        }
        ObjectMapper objectMapper = new ObjectMapper();
        TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {};
        return objectMapper.readValue(response.getBody(), typeRef);
    }

    /**
     * Estrae la chiave pubblica dal JWKS.
     *
     * @param jwks la mappa contenente il JWKS.
     * @return la chiave pubblica RSA estratta.
     * @throws Exception se si verifica un errore durante l'estrazione della chiave pubblica.
     */
    private RSAPublicKey getPublicKeyFromJwks(Map<String, Object> jwks) throws Exception {
        if (jwks == null || !jwks.containsKey("keys")) {
            throw new RuntimeException("Il JWKS non contiene il campo 'keys'");
        }
        ObjectMapper objectMapper = new ObjectMapper();
        List<Map<String, Object>> keys = objectMapper.convertValue(jwks.get("keys"), new TypeReference<List<Map<String, Object>>>() {});
        if (keys == null || keys.isEmpty()) {
            throw new RuntimeException("La lista 'keys' è vuota o null");
        }
        Map<String, Object> key = keys.get(0);
        BigInteger modulus = new BigInteger(1, Base64.getUrlDecoder().decode((String) key.get("n")));
        BigInteger exponent = new BigInteger(1, Base64.getUrlDecoder().decode((String) key.get("e")));
        LOGGER.info("Chiave pubblica generata con successo");
        return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(modulus, exponent));
    }
}