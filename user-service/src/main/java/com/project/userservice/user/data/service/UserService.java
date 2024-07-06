package com.project.userservice.user.data.service;

import com.project.userservice.exception.EntityNotFoundException;
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

    public List<UserEntity> getAllActiveUsers() {
        return userRepository.findAllActiveUsers();
    }

    public UserEntity getUserEntityById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("user with id '%s' is not found".formatted(id)));
    }

    public UserEntity registerUser(UserRequest userRequest) {
        UserEntity userEntityToSave = UserEntity.builder()
                .username(userRequest.getUsername())
                .password(bCryptPasswordEncoder.encode(userRequest.getPassword()))
                .email(userRequest.getEmail())
                .roles(List.of(Roles.ROLE_USER.toString()))
                .build();

        return userRepository.save(userEntityToSave);
    }

    public UserEntity updateUserEntity(String authorizationToken, UserRequest userRequest) {
        String usernameByToken = jwtUtils.getUsernameByToken(jwtUtils.extractTokenFromHeader(authorizationToken));
        UserEntity userEntityToAlter = userRepository.findByUsername(usernameByToken)
                .orElseThrow(() -> new EntityNotFoundException("user with id '%s' is not found".formatted(usernameByToken)));

        updateUserEntityFields(userEntityToAlter, userRequest);

        return userRepository.save(userEntityToAlter);
    }

    private void updateUserEntityFields(UserEntity user, UserRequest userRequest) {
        if (userRequest.getUsername() != null) {
            user.setUsername(user.getUsername());
        }

        if (userRequest.getEmail() != null) {
            user.setEmail(userRequest.getEmail());
        }
    }

    public void changePassword(String userId, String password) {
        UserEntity userEntityToChangePassword = getUserEntityById(userId);
        userEntityToChangePassword.setPassword(bCryptPasswordEncoder.encode(password));

        userRepository.save(userEntityToChangePassword);
    }
}
