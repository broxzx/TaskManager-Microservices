package com.project.userservice.exception.handler;

import com.project.userservice.exception.*;
import com.project.userservice.model.ErrorDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
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

    @ExceptionHandler(value = {MissingServletRequestParameterException.class})
    public ResponseEntity<ProblemDetail> handleMissingRequestParameterException(MissingServletRequestParameterException exception) {
        ProblemDetail problemDetail = ProblemDetail
                .forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());

        problemDetail.setProperty(OCCURRED, LocalDateTime.now());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(problemDetail);
    }

    @ExceptionHandler(value = {UserAlreadyExistsException.class, AuthorizationFailed.class, TokenInvalidException.class, ResetPasswordTokenIncorrectException.class})
    public ResponseEntity<ProblemDetail> handleCommonBadRequestExceptions(RuntimeException ex) {
        return buildExceptionHandling(ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {EntityNotFoundException.class})
    public ResponseEntity<ProblemDetail> handleCommonNotfoundException(RuntimeException ex) {
        return buildExceptionHandling(ex, HttpStatus.NOT_FOUND);
    }

    private static ResponseEntity<ProblemDetail> buildExceptionHandling(RuntimeException ex, HttpStatus httpStatus) {
        ProblemDetail problemDetail = ProblemDetail
                .forStatusAndDetail(httpStatus, ex.getMessage());

        problemDetail.setProperty(OCCURRED, LocalDateTime.now());

        return ResponseEntity
                .status(httpStatus)
                .body(problemDetail);
    }
}
