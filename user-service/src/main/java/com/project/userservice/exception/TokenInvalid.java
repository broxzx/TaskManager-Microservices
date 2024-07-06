package com.project.userservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class TokenInvalid extends RuntimeException {

    public TokenInvalid(String message) {
        super(message);
    }
}