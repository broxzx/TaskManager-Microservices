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

    private boolean emailVerified;

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

    private double taskCompletionRate;

    @Builder.Default
    private List<String> achievements = new ArrayList<>();

    private long points;

    private int level;

    private boolean isDeleted;

    @Builder.Default
    private List<String> roles = List.of(Roles.ROLE_USER.toString());

}
