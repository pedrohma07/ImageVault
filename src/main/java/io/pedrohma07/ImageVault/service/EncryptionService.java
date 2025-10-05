package io.pedrohma07.ImageVault.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class EncryptionService {

    @Value("${app.encryption.password}")
    private String password;

    @Value("${app.encryption.salt}")
    private String salt;

    private AesBytesEncryptor encryptor;

    @PostConstruct
    public void init() {
        this.encryptor = new AesBytesEncryptor(password, salt);
    }

    public String encrypt(String data) {
        byte[] encryptedBytes = encryptor.encrypt(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public String decrypt(String encryptedData) {
        byte[] decoded = Base64.getDecoder().decode(encryptedData);
        byte[] decryptedBytes = encryptor.decrypt(decoded);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}
