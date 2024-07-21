package com.project.userservice.user;

import com.project.userservice.model.ChangePasswordDto;
import com.project.userservice.model.TokenResponse;
import com.project.userservice.user.data.User;
import com.project.userservice.user.data.dto.request.LoginRequest;
import com.project.userservice.user.data.dto.request.UserRequest;
import com.project.userservice.user.service.UserService;
import com.project.userservice.utils.KafkaProducerService;
import com.project.userservice.utils.KeycloakUtils;
import jakarta.servlet.http.HttpServletResponse;
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
    public ResponseEntity<User> register(@RequestBody @Valid UserRequest userRequest) {
        return ResponseEntity.ok(userService.registerUser(userRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> loginUser(@RequestBody @Valid LoginRequest loginRequest) {
        return ResponseEntity.ok(keycloakUtils.getUserTokenFromUsernameAndPassword(loginRequest.getUsername(), loginRequest.getPassword(), loginRequest.isRememberMe()));
    }

    @GetMapping("/grantCode")
    public void processGrantCode(@RequestParam("code") String grantCode,
                                 @RequestParam("scope") String scope,
                                 @RequestParam("authuser") String authUser,
                                 @RequestParam("prompt") String prompt,
                                 HttpServletResponse response) {
        userService.processGrantCode(grantCode, response);
    }

    @PutMapping("/updateUserData")
    public ResponseEntity<User> updateUserData(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
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

    @GetMapping("/getUserIdByToken")
    public String getUserIdByToken(@RequestParam("token") String token) {
        return userService.getUserIdByToken(token);
    }

}
