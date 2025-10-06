package io.pedrohma07.ImageVault.service;

import io.pedrohma07.ImageVault.dto.user.CreateUserDTO;
import io.pedrohma07.ImageVault.dto.user.ResponseUserDTO;
import io.pedrohma07.ImageVault.dto.user.UpdateUserDTO;
import io.pedrohma07.ImageVault.exception.ResourceNotFoundException;
import io.pedrohma07.ImageVault.mapper.UserMapper;
import io.pedrohma07.ImageVault.model.User;
import io.pedrohma07.ImageVault.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final EncryptionService encryptionService;

    public ResponseUserDTO createUser(CreateUserDTO createUserDTO) {
        log.debug("Starting user creation for email: {}", createUserDTO.email());
        userRepository.findByEmail(createUserDTO.email()).ifPresent(user -> {
            log.warn("Attempted to create user with already registered email: {}", createUserDTO.email());
            throw new IllegalArgumentException("E-mail já cadastrado no sistema");
        });

        var newUser = User.builder()
                .name(createUserDTO.name())
                .email(createUserDTO.email())
                .password(passwordEncoder.encode(createUserDTO.password()))
                .build();

        User savedUser = userRepository.save(newUser);
        log.info("User created successfully with ID: {} and email: {}", savedUser.getId(), savedUser.getEmail());
        return userMapper.toResponseUserDTO(savedUser);
    }

    public Page<ResponseUserDTO> listAllUsers(Pageable pageable) {
        log.debug("Listing all users with pageable: {}", pageable);
        Page<User> users = userRepository.findAll(pageable);
        log.info("Found {} users on page {}", users.getNumberOfElements(), pageable.getPageNumber());
        return users.map(userMapper::toResponseUserDTO);
    }

    public ResponseUserDTO getUserById(String id) {
        log.debug("Fetching user by ID: {}", id);
        User user = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", id);
                    return new ResourceNotFoundException("Usuário não encontrado");
                });
        log.info("Successfully fetched user with ID: {}", id);
        return userMapper.toResponseUserDTO(user);
    }

    public ResponseUserDTO updateUser(String id, UpdateUserDTO updateUserDTO) {
        log.debug("Updating user with ID: {}", id);
        User user = userRepository.findById(java.util.UUID.fromString(id))
                .orElseThrow(() -> {
                    log.warn("User not found for update with ID: {}", id);
                    return new ResourceNotFoundException("Usuário não encontrado com o ID: " + id);
                });

        user.setName(updateUserDTO.name());

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully with ID: {}", id);
        return userMapper.toResponseUserDTO(updatedUser);
    }

    public void deleteUser(String id) {
        log.debug("Deleting user with ID: {}", id);
        UUID uuid = UUID.fromString(id);
        if (!userRepository.existsById(uuid)) {
            log.warn("Attempted to delete non-existent user with ID: {}", id);
            throw new ResourceNotFoundException("Usuário não encontrado com o ID: " + id);
        }
        userRepository.deleteById(uuid);
        log.info("User deleted successfully with ID: {}", id);
    }

    public void enableTwoFactorAuth(String userEmail, String secret) {
        log.debug("Enabling two-factor authentication for user: {}", userEmail);
        User user = userRepository.findByEmail(userEmail).orElseThrow(
                () -> {
                    log.warn("User not found for 2FA enablement with email: {}", userEmail);
                    return new ResourceNotFoundException("Usuário não encontrado com o e-mail: " + userEmail);
                }
        );

        user.setTwoFASecret(encryptionService.encrypt(secret));
        user.set2FAEnabled(true);
        userRepository.save(user);
        log.info("Two-factor authentication enabled for user: {}", userEmail);
    }
}
