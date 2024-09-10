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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

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
        log.info("user with name '%s' tried to log in".formatted(loginRequest.getUsername()));
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

    /**
     * Called when user changes data in his account
     *
     * @param userRequest         an object of type UserRequest that contains the user's data.
     *                            This object is expected to be validated using the @Valid annotation.
     * @param authorizationHeader an object of type String that contains access token that was given by authorization server
     * @return ResponseEntity<User> Returns a ResponseEntity containing the updated user data
     * and an HTTP status of 200 OK.
     */
    @PutMapping("/updateUserData")
    public ResponseEntity<User> updateUserData(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                               @RequestBody @Valid UserRequest userRequest) {
        return ResponseEntity.ok(userService.updateUserEntity(authorizationHeader, userRequest));
    }

    /**
     * Called when user wants to change his password
     *
     * @param userId an object of type String that represents user's id
     */
    @PostMapping("/forgotPassword")
    public void forgotPassword(@RequestParam("userId") String userId) {
        keycloakUtils.forgotPassword(userId);
    }

    /**
     * Called when user makes request to send email to change his password
     *
     * @param userEmail an object of type String that represents user's email
     */
    @PostMapping("/resetPassword")
    public void resetPassword(@RequestParam("email") String userEmail) {
        kafkaProducerService.sendForgotPasswordMail(userEmail);
    }

    /**
     * Called when user clicked on link from email
     *
     * @param token             an object of type String that was generated by backend to change user's password
     * @param changePasswordDto an object of type ChangePasswordDto that represents new user's credentials
     */
    @PostMapping("/changePassword")
    public void changePasswordWithNewPassword(@RequestParam("token") String token, @RequestBody ChangePasswordDto changePasswordDto) {
        userService.changePasswordWithNewPassword(token, changePasswordDto);
    }

    /**
     * Called when user's token expires
     *
     * @param refreshToken an object of type String that sent by client to renew token
     * @return ResponseEntity<TokenResponse> that provide user with new access and refresh tokens
     */
    @PostMapping("/refreshToken")
    public ResponseEntity<TokenResponse> refreshToken(@RequestParam("token") String refreshToken) {
        return ResponseEntity.ok(keycloakUtils.refreshToken(refreshToken));
    }

    /**
     * Usually used by feign clients to obtain user id using access token
     *
     * @param token represents access token
     * @return an object of type String that represents user's id
     */
    @PostMapping("/getUserIdByToken")
    public String getUserIdByToken(@RequestBody String token) {
        return userService.getUserIdByToken(token);
    }

    /**
     * Used by clients and feigns to check whether user with id already exists
     *
     * @param userId represents user's id
     * @return boolean that states whether user exists or not
     */
    @GetMapping("/checkUserExists")
    public boolean checkUserExists(@RequestParam("userId") String userId) {
        return userService.checkUserExists(userId);
    }

}
