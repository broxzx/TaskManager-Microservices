package com.project.projectservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class TokenNotValidException extends RuntimeException {

    public TokenNotValidException(String message) {
        super(message);
    }
}
