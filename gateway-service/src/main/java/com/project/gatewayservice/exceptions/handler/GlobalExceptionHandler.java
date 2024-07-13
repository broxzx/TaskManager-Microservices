package com.project.gatewayservice.exceptions.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpServerErrorException;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = {HttpServerErrorException.class})
    public ResponseEntity<ProblemDetail> handleHttpServerErrorException(MissingServletRequestParameterException exception) {
        ProblemDetail problemDetail = ProblemDetail
                .forStatusAndDetail(HttpStatus.NO_CONTENT, exception.getMessage());

        problemDetail.setProperty("occurred", LocalDateTime.now());

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body(problemDetail);
    }

}
