package com.example.app_service2.filter;

import com.example.app_service2.model.User;
import com.example.app_service2.service.UserService;
import com.example.app_service2.util.JwtUtil;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SecurityException;
import io.jsonwebtoken.UnsupportedJwtException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Filtro JWT che estende {@link OncePerRequestFilter} per elaborare le richieste HTTP.
 * <p>
 * Questo filtro intercetta le richieste in ingresso, estrae e valida il token JWT,
 * e imposta l'autenticazione nel contesto di sicurezza se il token è valido.
 * </p>
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = Logger.getLogger(JwtFilter.class.getName());

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    /**
     * Metodo principale del filtro che elabora ogni richiesta HTTP.
     *
     * @param request  la richiesta HTTP in ingresso.
     * @param response la risposta HTTP in uscita.
     * @param chain    la catena di filtri che consente di passare alla successiva.
     * @throws ServletException se si verifica un errore nel filtro.
     * @throws IOException      se si verifica un errore di I/O.
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        LOGGER.info("Header Authorization ricevuto: " + authorizationHeader);

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String jwt = authorizationHeader.substring(7);
            LOGGER.info("Token JWT estratto: " + jwt);
            try {
                Claims claims = jwtUtil.getClaims(jwt);
                String username = claims.getSubject();
                LOGGER.info("Nome utente estratto dal token: " + username);

                // Carica i dettagli dell'utente dal database
                User user = userService.findByUsername(username);

                // Verifica il token
                if (username != null && jwtUtil.validateToken(jwt, user)
                        && SecurityContextHolder.getContext().getAuthentication() == null) {

                    // Crea l'autenticazione basata sull'utente
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                    user.getUsername(), null, null); // Nessuna authority poiché non gestiamo i ruoli

                    authenticationToken.setDetails(
                            new org.springframework.security.web.authentication.WebAuthenticationDetailsSource()
                                    .buildDetails(request));

                    // Imposta l'autenticazione nel contesto di sicurezza
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                    LOGGER.info("Autenticazione impostata nel contesto di sicurezza per l'utente: " + username);
                }
            } catch (ExpiredJwtException | MalformedJwtException | SecurityException | UnsupportedJwtException e) {
                LOGGER.severe("Token JWT non valido: " + e.getMessage());
                throw new ServletException("Token JWT non valido.", e);
            } catch (IllegalArgumentException e) {
                LOGGER.severe("La stringa delle claim del token è vuota: " + e.getMessage());
                throw new ServletException("La stringa delle claim del token è vuota.", e);
            } catch (Exception e) {
                LOGGER.severe("Errore durante la validazione del token JWT: " + e.getMessage());
                throw new ServletException("Errore durante la validazione del token JWT.", e);
            }
        } else {
            LOGGER.warning("Token JWT mancante o header Authorization non valido");
        }

        // Prosegui con la catena di filtri
        chain.doFilter(request, response);
    }
}