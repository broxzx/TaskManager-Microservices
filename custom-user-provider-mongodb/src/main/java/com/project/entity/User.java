package com.project.entity;

import lombok.Data;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.util.List;

@Data
public class User {

    private ObjectId id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private LocalDate birthDate;
    private List<String> roles;
}
