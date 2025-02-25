package com.example.auth_service1.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.interfaces.RSAPublicKey;
import java.security.KeyPair;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller che espone la chiave pubblica in formato JWKS.
 */
@RestController
public class JwkSetController {

    private final RSAPublicKey publicKey;

    /**
     * Costruttore che riceve la chiave pubblica tramite iniezione delle dipendenze.
     *
     * @param keyPair La coppia di chiavi RSA.
     */
    public JwkSetController(KeyPair keyPair) {
        this.publicKey = (RSAPublicKey) keyPair.getPublic();
    }

    /**
     * Espone la chiave pubblica in formato JWKS.
     *
     * @return Una mappa contenente le chiavi pubbliche in formato JWKS.
     */
    @GetMapping("/oauth2/jwks")
    public Map<String, Object> getJwks() {
        // Costruzione del JWK
        Map<String, Object> jwk = new HashMap<>();
        jwk.put("kty", "RSA");
        jwk.put("alg", "RS256");
        jwk.put("use", "sig");
        jwk.put("kid", "exampleKeyId"); // In produzione, utilizza un ID univoco e gestisci la rotazione delle chiavi
        jwk.put("n", Base64.getUrlEncoder().withoutPadding().encodeToString(publicKey.getModulus().toByteArray()));
        jwk.put("e", Base64.getUrlEncoder().withoutPadding().encodeToString(publicKey.getPublicExponent().toByteArray()));

        // Aggiunta del JWK alla lista delle chiavi
        Map<String, Object> response = new HashMap<>();
        response.put("keys", List.of(jwk));

        System.out.println("Accesso all'endpoint /oauth2/jwks in JwkSetController");

        return response;
    }
}
