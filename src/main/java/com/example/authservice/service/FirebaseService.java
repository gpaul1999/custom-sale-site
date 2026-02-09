package com.example.authservice.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;

@Service
public class FirebaseService {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseService.class);

    @Value("${firebase.enabled:false}")
    private boolean firebaseEnabled;

    @Value("${firebase.service-account-path:}")
    private String serviceAccountPath;

    @PostConstruct
    public void initialize() {
        if (!firebaseEnabled) {
            logger.info("Firebase is disabled");
            return;
        }

        try {
            if (serviceAccountPath == null || serviceAccountPath.isEmpty()) {
                logger.warn("Firebase is enabled but service account path is not configured");
                return;
            }

            FileInputStream serviceAccount = new FileInputStream(serviceAccountPath);
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                logger.info("Firebase initialized successfully");
            }
        } catch (Exception e) {
            logger.error("Failed to initialize Firebase: {}", e.getMessage());
        }
    }

    public FirebaseToken verifyToken(String idToken) throws Exception {
        if (!firebaseEnabled) {
            throw new IllegalStateException("Firebase is not enabled");
        }
        return FirebaseAuth.getInstance().verifyIdToken(idToken);
    }
}
