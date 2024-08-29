package com.project.taskservice.exceptions.handler;

import com.project.taskservice.exceptions.EntityNotFoundException;
import com.project.taskservice.exceptions.ForbiddenException;
import com.project.taskservice.exceptions.TokenInvalidException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = {TokenInvalidException.class, EntityNotFoundException.class})
    public ResponseEntity<ProblemDetail> handleCommonBadRequestExceptions(RuntimeException ex) {
        return buildCommonExceptionHandler(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {ForbiddenException.class})
    public ResponseEntity<ProblemDetail> handleCommonForbiddenExceptions(RuntimeException ex) {
        return buildCommonExceptionHandler(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    public ResponseEntity<ProblemDetail> buildCommonExceptionHandler(String message, HttpStatus status) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, message);

        problemDetail.setProperty("occurred", LocalDateTime.now());

        return ResponseEntity.status(status)
                .body(problemDetail);
    }

}
