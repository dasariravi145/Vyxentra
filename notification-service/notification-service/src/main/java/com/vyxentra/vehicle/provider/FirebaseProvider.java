package com.vyxentra.vehicle.provider;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class FirebaseProvider implements PushProvider {

    @Value("${firebase.service-account-file}")
    private String serviceAccountFile;

    @PostConstruct
    public void initialize() {
        try {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(
                            new ClassPathResource(serviceAccountFile).getInputStream()))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("Firebase initialized successfully");
            }
        } catch (IOException e) {
            log.error("Failed to initialize Firebase: {}", e.getMessage());
        }
    }

    @Override
    public Map<String, Object> sendPush(String deviceToken, String title, String body,
                                        Map<String, String> data, String imageUrl, String clickAction) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Build notification
            Notification.Builder notificationBuilder = Notification.builder()
                    .setTitle(title)
                    .setBody(body);

            if (imageUrl != null) {
                notificationBuilder.setImage(imageUrl);
            }

            // Build message
            Message.Builder messageBuilder = Message.builder()
                    .setToken(deviceToken)
                    .setNotification(notificationBuilder.build());

            // Add data payload
            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            // Add Android config
            AndroidConfig.Builder androidConfig = AndroidConfig.builder();
            if (clickAction != null) {
                androidConfig.setPriority(AndroidConfig.Priority.HIGH)
                        .setNotification(AndroidNotification.builder()
                                .setClickAction(clickAction)
                                .build());
            }
            messageBuilder.setAndroidConfig(androidConfig.build());

            // Add APNS config for iOS
            Aps aps = Aps.builder()
                    .setSound("default")
                    .build();
            messageBuilder.setApnsConfig(ApnsConfig.builder()
                    .setAps(aps)
                    .build());

            Message message = messageBuilder.build();

            // Send message
            String messageId = FirebaseMessaging.getInstance().send(message);

            response.put("success", true);
            response.put("messageId", messageId);

            log.info("Firebase push sent with ID: {}", messageId);

        } catch (FirebaseMessagingException e) {
            log.error("Firebase push failed: {}", e.getMessage());
            response.put("success", false);
            response.put("error", e.getErrorCode() != null ? e.getErrorCode().name() : e.getMessage());
        }

        return response;
    }

    @Override
    public String getProviderName() {
        return "FIREBASE";
    }
}
