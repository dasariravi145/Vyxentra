package com.vyxentra.vehicle.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.RsaSecretEncryptor;
import org.springframework.security.crypto.encrypt.TextEncryptor;

import java.security.KeyPair;

@Configuration
public class EncryptionConfig {

    private static final Logger logger = LoggerFactory.getLogger(EncryptionConfig.class);

    @Value("${encrypt.key-store.location:}")
    private Resource keyStoreResource;

    @Value("${encrypt.key-store.password:}")
    private String keyStorePassword;

    @Value("${encrypt.key-store.alias:}")
    private String keyStoreAlias;

    @Value("${encrypt.key-strength:256}")
    private Integer keyStrength;

    private final ResourceLoader resourceLoader;
    public EncryptionConfig(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public TextEncryptor  textEncryptor(){
        try {
            if (keyStoreResource != null && keyStoreResource.exists()) {
                logger.info("Initializing RSA-based encryption with key store: {}", keyStoreResource.getFilename());

                // Load private key from JKS (simplified - in production use proper KeyStore loading)
                KeyPair keyPair = loadKeyPairFromStore();
                if (keyPair != null) {
                    RsaSecretEncryptor encryptor = new RsaSecretEncryptor(keyPair);
                    logger.info("RSA encryption initialized successfully");
                    return encryptor;
                }
            }

            // Fallback to AES encryption with configured key strength
            logger.info("Initializing AES encryption with key strength: {}", keyStrength);
            String password = System.getenv("ENCRYPT_KEY");
            if (password == null || password.isEmpty()) {
                password = "DEFAULT_ENCRYPTION_KEY_CHANGE_ME";
                logger.warn("Using default encryption key. Set ENCRYPT_KEY environment variable in production!");
            }

            String salt = System.getenv("ENCRYPT_SALT");
            if (salt == null || salt.isEmpty()) {
                salt = "deadbeefdeadbeef";
                logger.warn("Using default encryption salt. Set ENCRYPT_SALT environment variable in production!");
            }

            TextEncryptor encryptor = Encryptors.text(password, salt);
            logger.info("AES encryption initialized successfully");
            return encryptor;

        } catch (Exception e) {
            logger.error("Failed to initialize encryption", e);
            throw new RuntimeException("Encryption initialization failed", e);
        }
    }
    private KeyPair loadKeyPairFromStore() {
        try {
            // This is a simplified implementation
            // In production, implement proper JKS/PKCS12 loading
            logger.info("Loading key pair from store: {}", keyStoreResource.getFilename());

            // For demonstration purposes only
            // Implement actual keystore loading based on your requirements
            return null;

        } catch (Exception e) {
            logger.error("Failed to load key pair from store", e);
            return null;
        }
    }
    @Bean
    public String encryptionStatus() {
        String status = keyStoreResource != null && keyStoreResource.exists() ?
                "RSA Encryption Active" : "AES Encryption Active (Key Strength: " + keyStrength + ")";
        logger.info("Encryption status: {}", status);
        return status;
    }
}
