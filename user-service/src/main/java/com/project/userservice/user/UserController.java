package com.project.userservice.user;

import com.project.userservice.model.ChangePasswordDto;
import com.project.userservice.model.TokenResponse;
import com.project.userservice.user.data.UserEntity;
import com.project.userservice.user.data.dto.request.LoginRequest;
import com.project.userservice.user.data.dto.request.UserRequest;
import com.project.userservice.user.service.UserService;
import com.project.userservice.utils.KafkaProducerService;
import com.project.userservice.utils.KeycloakUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor

public class UserController {

    private final UserService userService;
    private final KeycloakUtils keycloakUtils;
    private final KafkaProducerService kafkaProducerService;

    @PostMapping("/register")
    public ResponseEntity<UserEntity> register(@RequestBody @Valid UserRequest userRequest) {
        return ResponseEntity.ok(userService.registerUser(userRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> loginUser(@RequestBody @Valid LoginRequest loginRequest) {
        return ResponseEntity.ok(keycloakUtils.getUserTokenFromUsernameAndPassword(loginRequest.getUsername(), loginRequest.getPassword(), loginRequest.isRememberMe()));
    }


    @PutMapping("/updateUserData")
    public ResponseEntity<UserEntity> updateUserData(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                                     @RequestBody @Valid UserRequest userRequest) {
        return ResponseEntity.ok(userService.updateUserEntity(authorizationHeader, userRequest));
    }

    @PostMapping("/forgotPassword")
    public void forgotPassword(@RequestParam("userId") String userId) {
        keycloakUtils.forgotPassword(userId);
    }

    @PostMapping("/resetPassword")
    public void resetPassword(@RequestParam("email") String userEmail) {
        kafkaProducerService.sendForgotPasswordMail(userEmail);
    }

    @PostMapping("/changePassword")
    public void changePasswordWithNewPassword(@RequestParam("token") String token, @RequestBody ChangePasswordDto changePasswordDto) {
        userService.changePasswordWithNewPassword(token, changePasswordDto);
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<TokenResponse> refreshToken(@RequestParam("token") String refreshToken) {
        return ResponseEntity.ok(keycloakUtils.refreshToken(refreshToken));
    }

}
