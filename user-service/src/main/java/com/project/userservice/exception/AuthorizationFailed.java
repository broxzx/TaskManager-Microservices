package com.project.userservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AuthorizationFailed extends RuntimeException {

    public AuthorizationFailed(String message) {
        super(message);
    }

}
