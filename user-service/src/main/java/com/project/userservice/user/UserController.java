package com.project.userservice.user;

import com.project.userservice.model.ChangePasswordDto;
import com.project.userservice.model.TokenResponse;
import com.project.userservice.user.data.User;
import com.project.userservice.user.data.dto.request.LoginRequest;
import com.project.userservice.user.data.dto.request.UserRequest;
import com.project.userservice.user.service.UserService;
import com.project.userservice.utils.KafkaProducerService;
import com.project.userservice.utils.KeycloakUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final KeycloakUtils keycloakUtils;
    private final KafkaProducerService kafkaProducerService;

    /**
     * Called when user submitted form in /register page
     *
     * @param userRequest an object of type UserRequest that contains the user's registration data.
     *                    This object is expected to be validated using the @Valid annotation.
     * @return ResponseEntity<User> Returns a ResponseEntity containing the registered user object
     * and an HTTP status of 200 OK.
     */
    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody @Valid UserRequest userRequest) {
        return ResponseEntity.ok(userService.registerUser(userRequest));
    }

    /**
     * Called when user submitted his credentials in /login page
     *
     * @param loginRequest an object of type LoginRequest that contains the user's login data.
     *                     This object is expected to be validated using the @Valid annotation.
     * @return ResponseEntity<TokenResponse> Returns a ResponseEntity containing the token response (including access_token
     * and refresh_token) and an HTTP status of 200 OK.
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> loginUser(@RequestBody @Valid LoginRequest loginRequest) {
        logger.info("user with name '%s' tried to log in".formatted(loginRequest.getUsername()));
        return ResponseEntity.ok(keycloakUtils.getUserTokenFromUsernameAndPassword(loginRequest.getUsername(), loginRequest.getPassword(), loginRequest.isRememberMe()));
    }

    /**
     * Called when user logins by google account.
     *
     * @param grantCode an object of type String that contains the code that was sent from Google authorization server
     */
    @GetMapping("/grantCode")
    public ResponseEntity<TokenResponse> processGrantCode(@RequestParam("code") String grantCode) {
        return ResponseEntity.ok(userService.processGrantCode(grantCode));
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

    @GetMapping("checkUserExists")
    public boolean checkUserExists(@RequestParam("userId") String userId) {
        return userService.checkUserExists(userId);
    }

}
