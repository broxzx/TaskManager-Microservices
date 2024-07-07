package com.project.userservice.exception.handler;

import com.project.userservice.exception.AuthorizationFailed;
import com.project.userservice.exception.EntityNotFoundException;
import com.project.userservice.exception.TokenInvalid;
import com.project.userservice.exception.UserAlreadyExists;
import com.project.userservice.model.ErrorDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final static String OCCURRED = "occurred";

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public ResponseEntity<ProblemDetail> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail
                .forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());

        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();

        List<ErrorDto> errors = fieldErrors.stream()
                .map(error -> new ErrorDto(error.getField(), error.getDefaultMessage()))
                .toList();

        problemDetail.setProperty(OCCURRED, LocalDateTime.now());
        problemDetail.setProperty("path", request.getRequestURI());
        problemDetail.setProperty("errors", errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(problemDetail);
    }

    @ExceptionHandler(value = {UserAlreadyExists.class, AuthorizationFailed.class, TokenInvalid.class})
    public ResponseEntity<ProblemDetail> handleCommonBadRequestExceptions(RuntimeException ex, HttpServletRequest request) {
        return buildExceptionHandling(ex, request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {EntityNotFoundException.class})
    public ResponseEntity<ProblemDetail> handleCommonNotfoundException(RuntimeException ex, HttpServletRequest request) {
        return buildExceptionHandling(ex, request, HttpStatus.NOT_FOUND);
    }

    private static ResponseEntity<ProblemDetail> buildExceptionHandling(RuntimeException ex, HttpServletRequest request, HttpStatus httpStatus) {
        ProblemDetail problemDetail = ProblemDetail
                .forStatusAndDetail(httpStatus, ex.getMessage());

        problemDetail.setProperty(OCCURRED, LocalDateTime.now());
        problemDetail.setProperty("path", request.getRequestURI());

        return ResponseEntity
                .status(httpStatus)
                .body(problemDetail);
    }
}
