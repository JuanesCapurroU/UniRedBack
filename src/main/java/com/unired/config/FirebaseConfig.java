package com.unired.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@Configuration
@ConditionalOnProperty(prefix = "firebase", name = "enabled", havingValue = "true")
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.credentials-path}")
    private String credentialsPath;

    @Bean
    public FirebaseApp firebaseApp(ResourceLoader resourceLoader) throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }

        Resource resource = resourceLoader.getResource(credentialsPath);
        if (!resource.exists()) {
            throw new IOException("Firebase credentials not found at path: " + credentialsPath);
        }

        GoogleCredentials credentials = GoogleCredentials
                .fromStream(resource.getInputStream())
                .createScoped("https://www.googleapis.com/auth/cloud-platform");

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build();

        log.info("Firebase initialized successfully");
        return FirebaseApp.initializeApp(options);
    }
}
