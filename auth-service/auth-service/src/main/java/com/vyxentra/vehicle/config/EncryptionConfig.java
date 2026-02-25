package com.vyxentra.vehicle.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

@Configuration
public class EncryptionConfig {

    @Bean
    public TextEncryptor textEncryptor() {
        String password = System.getenv("ENCRYPTION_PASSWORD");
        String salt = System.getenv("ENCRYPTION_SALT");

        if (password == null || salt == null) {
            // For development only - use proper encryption in production
            return Encryptors.noOpText();
        }

        return Encryptors.text(password, salt);
    }
}
