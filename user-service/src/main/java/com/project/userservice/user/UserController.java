package com.project.userservice.user;

import com.project.userservice.user.data.UserEntity;
import com.project.userservice.user.data.dto.request.LoginRequest;
import com.project.userservice.user.data.dto.request.UserRequest;
import com.project.userservice.user.data.service.UserService;
import com.project.userservice.utils.KeycloakUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final KeycloakUtils keycloakUtils;

    @GetMapping("/register")
    public ResponseEntity<UserEntity> register(@RequestBody @Valid UserRequest userRequest) {
        return ResponseEntity.ok(userService.registerUser(userRequest));
    }

    @GetMapping("/login")
    public String loginUser(@RequestBody @Valid LoginRequest loginRequest) {
        return keycloakUtils.getUserTokenFromUsernameAndPassword(loginRequest.getUsername(), loginRequest.getPassword());
    }


    @PutMapping("/updateUserData/{userId}")
    public ResponseEntity<UserEntity> updateUserData(@PathVariable("userId") String userId,
                                                     @RequestBody @Valid UserRequest userRequest) {
        return ResponseEntity.ok(userService.updateUserEntity(userId, userRequest));
    }


}
