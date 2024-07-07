package com.project.userservice.model;

import com.project.userservice.user.data.dto.response.UserResponse;

public record TokenResponse(String accessToken, String refreshToken, Long expiresIn, UserResponse userResponse) {
}
