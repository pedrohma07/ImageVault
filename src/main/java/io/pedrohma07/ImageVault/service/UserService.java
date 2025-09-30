package io.pedrohma07.ImageVault.service;

import io.pedrohma07.ImageVault.dto.user.CreateUserDTO;
import io.pedrohma07.ImageVault.dto.user.ResponseUserDTO;
import io.pedrohma07.ImageVault.dto.user.UpdateUserDTO;
import io.pedrohma07.ImageVault.mapper.UserMapper;
import io.pedrohma07.ImageVault.model.User;
import io.pedrohma07.ImageVault.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Transactional
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public ResponseUserDTO createUser(CreateUserDTO createUserDTO) {
        userRepository.findByEmail(createUserDTO.email()).ifPresent(user -> {
            throw new IllegalArgumentException("E-mail já cadastrado no sistema");
        });

        var newUser = User.builder()
                .name(createUserDTO.name())
                .email(createUserDTO.email())
                .password(createUserDTO.password())
                .build();

        User savedUser = userRepository.save(newUser);
        return userMapper.toResponseUserDTO(savedUser);
    }

    public Page<ResponseUserDTO> listAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return users.map(userMapper::toResponseUserDTO);
    }

    public ResponseUserDTO getUserById(String id) {
        User user = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        return userMapper.toResponseUserDTO(user);
    }

    public ResponseUserDTO updateUser(String id, UpdateUserDTO updateUserDTO) {
        User user = userRepository.findById(java.util.UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com o ID: " + id));

        user.setName(updateUserDTO.name());

        User updatedUser = userRepository.save(user);
        return userMapper.toResponseUserDTO(updatedUser);
    }

    public void deleteUser(String id) {
        UUID uuid = UUID.fromString(id);
        if (!userRepository.existsById(uuid)) {
            throw new RuntimeException("Usuário não encontrado com o ID: " + id);
        }
        userRepository.deleteById(uuid);
    }
}
