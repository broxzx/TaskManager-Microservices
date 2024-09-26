package com.project.userservice.utils;

import com.project.userservice.model.TokenResponse;
import com.project.userservice.user.data.dto.response.UserResponse;

import java.util.Date;
import java.util.List;

public class SecurityUtils {

    public static String authorizationHeader() {
        return "Bearer mocked-jwt-token";
    }

    public static TokenResponse tokenResponse() {
        return new TokenResponse("token", "refresh_token",
                new Date().getTime() + 3600, new UserResponse("user", "user@mail.com", List.of("ROLE_USER")));
    }

}
