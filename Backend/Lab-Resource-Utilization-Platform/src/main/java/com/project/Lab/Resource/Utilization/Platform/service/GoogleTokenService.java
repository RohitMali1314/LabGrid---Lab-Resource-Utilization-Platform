package com.project.Lab.Resource.Utilization.Platform.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class GoogleTokenService {

    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenService(
            @Value("${google.client-id}") String clientId
    ) {

        if (clientId == null || clientId.isBlank()) {
            throw new IllegalStateException(
                    "Google client ID is not configured"
            );
        }

        this.verifier =
                new GoogleIdTokenVerifier.Builder(
                        new NetHttpTransport(),
                        GsonFactory.getDefaultInstance()
                )
                        .setAudience(
                                Collections.singletonList(clientId)
                        )
                        .build();
    }

    // ==========================================================
    // VERIFY GOOGLE ID TOKEN
    // ==========================================================

    public GoogleIdToken.Payload verifyToken(
            String credential
    ) {

        if (credential == null ||
                credential.isBlank()) {

            throw new RuntimeException(
                    "Google credential is missing"
            );
        }

        try {

            GoogleIdToken idToken =
                    verifier.verify(credential);

            if (idToken == null) {

                throw new RuntimeException(
                        "Invalid Google ID token"
                );
            }

            GoogleIdToken.Payload payload =
                    idToken.getPayload();

            // --------------------------------------------------
            // VERIFY EMAIL
            // --------------------------------------------------

            if (payload.getEmail() == null ||
                    payload.getEmail().isBlank()) {

                throw new RuntimeException(
                        "Google account email is unavailable"
                );
            }

            // --------------------------------------------------
            // VERIFY EMAIL VERIFIED
            // --------------------------------------------------

            if (!Boolean.TRUE.equals(
                    payload.getEmailVerified()
            )) {

                throw new RuntimeException(
                        "Google email is not verified"
                );
            }

            // --------------------------------------------------
            // VERIFY ISSUER
            // --------------------------------------------------

            String issuer =
                    payload.getIssuer();

            if (!"https://accounts.google.com".equals(issuer) &&
                    !"accounts.google.com".equals(issuer)) {

                throw new RuntimeException(
                        "Invalid Google token issuer"
                );
            }

            return payload;

        } catch (RuntimeException e) {

            throw e;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Google authentication failed"
            );
        }
    }
}