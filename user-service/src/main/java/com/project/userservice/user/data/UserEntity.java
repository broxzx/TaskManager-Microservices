package com.project.userservice.user.data;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Document(collection = "users")
public class UserEntity {

    private String id;

    private String username;

    private String password;

    @Email
    private String email;

    @Builder.Default
    private Boolean emailVerified = false;

    @Builder.Default
    private Boolean isDeleted = false;

    private List<String> roles = new ArrayList<>();

}
