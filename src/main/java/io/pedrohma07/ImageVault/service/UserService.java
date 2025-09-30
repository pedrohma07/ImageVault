package io.pedrohma07.ImageVault.service;

import io.pedrohma07.ImageVault.dto.user.CreateUserDTO;
import io.pedrohma07.ImageVault.dto.user.ResponseUserDTO;
import io.pedrohma07.ImageVault.model.User;
import io.pedrohma07.ImageVault.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public ResponseUserDTO createUser(CreateUserDTO createUserDTO) {
        var newUser = User.builder()
                .name(createUserDTO.name())
                .email(createUserDTO.email())
                .password(createUserDTO.password())
                .build();

        User savedUser = userRepository.save(newUser);

        return new ResponseUserDTO(
                savedUser.getId().toString(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.is2FAEnabled(),
                savedUser.getCreatedAt().toString(),
                savedUser.getUpdatedAt().toString()
        );
    }


}
