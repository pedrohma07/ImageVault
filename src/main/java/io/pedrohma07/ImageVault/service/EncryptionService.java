package io.pedrohma07.ImageVault.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class EncryptionService {

    private static final Logger log = LoggerFactory.getLogger(EncryptionService.class);

    @Value("${app.encryption.password}")
    private String password;

    @Value("${app.encryption.salt}")
    private String salt;

    private AesBytesEncryptor encryptor;

    @PostConstruct
    public void init() {
        log.debug("Initializing EncryptionService.");
        this.encryptor = new AesBytesEncryptor(password, salt);
        log.debug("EncryptionService initialized successfully.");
    }

    public String encrypt(String data) {
        log.debug("Encrypting data.");
        try {
            byte[] encryptedBytes = encryptor.encrypt(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            log.error("Error during data encryption.", e);
            throw e;
        }
    }

    public String decrypt(String encryptedData) {
        log.debug("Decrypting data.");
        try {
            byte[] decoded = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedBytes = encryptor.decrypt(decoded);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Error during data decryption. This could be due to invalid encrypted data or incorrect password/salt.", e);
            throw new RuntimeException("Failed to decrypt data", e);
        }
    }
}
