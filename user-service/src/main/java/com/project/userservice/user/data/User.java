package com.project.userservice.user.data;

import com.project.userservice.user.data.enums.Roles;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Document(collection = "users")
public class User {

    private String id;

    private String username;

    private String password;

    private String email;

    @Builder.Default
    private Boolean emailVerified = false;

    private String firstName;

    private String lastName;

    private LocalDate birthDate;

    private String profilePictureUrl;

    private String googleAccountId;

    @Builder.Default
    private boolean calendarSyncEnabled = false;

    @Builder.Default
    private LocalDateTime createdDate = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime lastLoginDate = LocalDateTime.now();

    @Builder.Default
    private Double taskCompletionRate = 0.0D;

    @Builder.Default
    private List<String> achievements = new ArrayList<>();

    @Builder.Default
    private Long points = 0L;

    @Builder.Default
    private Integer level = 0;

    @Builder.Default
    private Boolean isDeleted = false;

    @Builder.Default
    private List<String> roles = List.of(Roles.ROLE_USER.toString());

}
