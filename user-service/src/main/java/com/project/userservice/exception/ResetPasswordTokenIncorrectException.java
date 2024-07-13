package com.project.userservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ResetPasswordTokenIncorrectException extends RuntimeException {

    public ResetPasswordTokenIncorrectException(String message) {
        super(message);
    }
}
