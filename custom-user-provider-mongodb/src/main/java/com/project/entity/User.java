package com.project.entity;

import lombok.Data;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class User {

    private ObjectId id;
    private String username;
    private String password;
    private String email;
    private boolean emailVerified;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String profilePictureUrl;
    private String googleAccountId;
    private boolean calendarSyncEnabled;
    private LocalDateTime createdDate;
    private LocalDateTime lastLoginDate;
    private double taskCompletionRate;
    private List<String> achievements;
    private long points;
    private int level;
    private boolean isDeleted;
    private List<String> roles;

}
