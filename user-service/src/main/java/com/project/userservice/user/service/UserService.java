package com.project.userservice.user.service;

import com.project.userservice.exception.EntityNotFoundException;
import com.project.userservice.exception.UserAlreadyExists;
import com.project.userservice.user.data.UserEntity;
import com.project.userservice.user.data.dto.request.UserRequest;
import com.project.userservice.user.data.enums.Roles;
import com.project.userservice.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserEntity getUserEntityById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("user with id '%s' is not found".formatted(id)));
    }

    public UserEntity registerUser(UserRequest userRequest) {
        checkUserDuplicates(userRequest);

        UserEntity userEntityToSave = UserEntity.builder()
                .username(userRequest.getUsername())
                .password(bCryptPasswordEncoder.encode(userRequest.getPassword()))
                .email(userRequest.getEmail())
                .firstName(userRequest.getFirstName())
                .lastName(userRequest.getLastName())
                .birthDate(userRequest.getBirthDate())
                .roles(List.of(Roles.ROLE_USER.toString()))
                .build();

        return userRepository.save(userEntityToSave);
    }

    public UserEntity updateUserEntity(String authorizationToken, UserRequest userRequest) {
        String usernameByToken = jwtUtils.getUsernameByToken(jwtUtils.extractTokenFromHeader(authorizationToken));
        UserEntity userEntityToAlter = getUserEntityByUsername(usernameByToken);

        updateUserEntityFields(userEntityToAlter, userRequest);

        return userRepository.save(userEntityToAlter);
    }

    public void changePassword(String userId, String password) {
        UserEntity userEntityToChangePassword = getUserEntityById(userId);
        userEntityToChangePassword.setPassword(bCryptPasswordEncoder.encode(password));

        userRepository.save(userEntityToChangePassword);
    }

    public UserEntity getUserEntityByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("user with id '%s' is not found".formatted(username)));
    }

    private void checkUserDuplicates(UserRequest userRequest) {
        userRepository.findByUsername(userRequest.getUsername())
                .ifPresent(user -> {
                    throw new UserAlreadyExists("user with username '%s' already exists".formatted(userRequest.getUsername()));
                });

        userRepository.findByEmail(userRequest.getEmail())
                .ifPresent(user -> {
                    throw new UserAlreadyExists("user with email '%s' already exists".formatted(userRequest.getEmail()));
                });
    }

    private void updateUserEntityFields(UserEntity user, UserRequest userRequest) {
        if (userRequest.getUsername() != null) {
            user.setUsername(user.getUsername());
        }

        if (userRequest.getEmail() != null) {
            user.setEmail(userRequest.getEmail());
        }
    }
}
