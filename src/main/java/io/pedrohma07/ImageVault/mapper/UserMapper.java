package io.pedrohma07.ImageVault.mapper;

import io.pedrohma07.ImageVault.dto.user.ResponseUserDTO;
import io.pedrohma07.ImageVault.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public ResponseUserDTO toResponseUserDTO(User user) {
        return new ResponseUserDTO(
                user.getId().toString(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.is2FAEnabled(),
                user.getCreatedAt().toString(),
                user.getUpdatedAt().toString()
        );
    }
}