package com.project.projectservice.exceptions.handler;

import com.project.projectservice.exceptions.DefaultException;
import com.project.projectservice.exceptions.EntityNotFoundException;
import com.project.projectservice.exceptions.ForbiddenException;
import com.project.projectservice.exceptions.TokenNotValidException;
import com.project.projectservice.utils.ErrorDto;
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

    @ExceptionHandler(value = {})
    public ResponseEntity<ProblemDetail> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex,
                                                                               HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail
                .forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());

        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();

        List<ErrorDto> errors = fieldErrors.stream()
                .map(error -> new ErrorDto(error.getField(), error.getDefaultMessage()))
                .toList();

        problemDetail.setProperty("occurred", LocalDateTime.now());
        problemDetail.setProperty("path", request.getRequestURI());
        problemDetail.setProperty("errors", errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(problemDetail);
    }


    @ExceptionHandler(value = {TokenNotValidException.class, EntityNotFoundException.class, ForbiddenException.class})
    public ResponseEntity<ProblemDetail> handleBadRequestExceptions(RuntimeException ex) {
        return buildExceptionHandling(ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {DefaultException.class})
    public ResponseEntity<ProblemDetail> handleInternalServerErrorExceptions(RuntimeException exception) {
        return buildExceptionHandling(exception, HttpStatus.INTERNAL_SERVER_ERROR);
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
