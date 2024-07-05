package com.project.userservice.user.data.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserResponse {
    private String username;
    private String email;
    private List<String> roles;
}
