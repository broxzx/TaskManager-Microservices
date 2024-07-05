package com.project.userservice.user.data.service;

import com.project.userservice.exception.EntityNotFoundException;
import com.project.userservice.user.data.UserEntity;
import com.project.userservice.user.data.dto.request.UserRequest;
import com.project.userservice.user.data.enums.Roles;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
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

    public UserEntity updateUserEntity(String userId, UserRequest userRequest) {
        UserEntity userEntityToAlter = getUserEntityById(userId);

        userEntityToAlter.setUsername(userRequest.getUsername());
        userEntityToAlter.setEmail(userRequest.getEmail());

        return userRepository.save(userEntityToAlter);
    }

    public void changePassword(String userId, String password) {
        UserEntity userEntityToChangePassword = getUserEntityById(userId);
        userEntityToChangePassword.setPassword(bCryptPasswordEncoder.encode(password));

        userRepository.save(userEntityToChangePassword);
    }

}
