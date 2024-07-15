package com.project.projectservice.exceptions.handler;

import com.project.projectservice.exceptions.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = {EntityNotFoundException.class})
    public ResponseEntity<ProblemDetail> handleCommonNotFoundExceptions(RuntimeException ex) {
        return buildExceptionHandling(ex, HttpStatus.NOT_FOUND);
    }

    private static ResponseEntity<ProblemDetail> buildExceptionHandling(RuntimeException ex, HttpStatus httpStatus) {
        ProblemDetail problemDetail = ProblemDetail
                .forStatusAndDetail(httpStatus, ex.getMessage());

        problemDetail.setProperty("occurred", LocalDateTime.now());

        return ResponseEntity
                .status(httpStatus)
                .body(problemDetail);
    }
}
