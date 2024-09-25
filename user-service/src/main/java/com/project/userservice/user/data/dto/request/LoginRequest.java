package com.project.userservice.user.data.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class LoginRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    private boolean rememberMe;

}
