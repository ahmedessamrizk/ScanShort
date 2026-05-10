package com.demo.exceptions;

import com.demo.exceptions.custom.*;
import io.jsonwebtoken.JwtException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.demo.utils.*;

import javax.naming.AuthenticationException;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceGoneException.class)
    public ResponseEntity<?> handleResourceGoneException(ResourceGoneException ex) {
        return ResponseEntity.status(HttpStatus.GONE)
                .body(ApiErrorResponse.of(ex.getMessage(), "RESOURCE_PERMANENTLY_DELETED"));
    }

    @ExceptionHandler(TooManyRequestException.class)
    public ResponseEntity<?> handleTooManyRequestsException(TooManyRequestException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ApiErrorResponse.of(ex.getMessage(), "TOO_MANY_REQUESTS"));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<?> handleValidationException(ValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(ex.getMessage(), "VALIDATION_FAILED"));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiErrorResponse.of(ex.getMessage(), "AUTHENTICATION_FAILED"));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiErrorResponse> handleForbiddenException(ForbiddenException ex) {

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiErrorResponse.of(ex.getMessage(), "FORBIDDEN"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(ex.getMessage(), "ILLEGAL_ARGUMENT_EXCEPTION"));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorizedException(UnauthorizedException ex) {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiErrorResponse.of( ex.getMessage(), "AUTHENTICATION_FAILED"));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalStateException(IllegalStateException ex) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of( ex.getMessage(), "ILLEGAL_STATE_EXCEPTION"));
    }

    //Validation Exception for body
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        List<FieldError> errors = new ArrayList<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.add(FieldError.builder().field(error.getField()).issue(error.getDefaultMessage()).build())
        );
        return ResponseEntity.badRequest().body(ApiErrorResponse.builder().message("Validation error").errorCode("VALIDATION_ERROR").errors(errors).build());
    }

    //Validation Exception for path variable and queries
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handlePathValidation(ConstraintViolationException ex) {
        List<FieldError> errors = new ArrayList<>();

        ex.getConstraintViolations().forEach(v ->
                errors.add(FieldError.builder()
                        .field(v.getPropertyPath().toString().substring(v.getPropertyPath().toString().indexOf('.')+1))
                        .issue(v.getMessage())
                        .build())
        );

        return ResponseEntity.badRequest().body(ApiErrorResponse.builder().message("Invalid path parameters").errorCode("INVALID_PATH").errors(errors).build());
    }

    //Conflict exception
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ConflictException ex) {

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of(ex.getMessage(), "CONFLICT"));
    }

    //NotFound exception
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(NotFoundException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of(ex.getMessage(), "NOT_FOUND"));
    }

    //General Exception handler
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneral(Exception ex) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.of(ex.getMessage(), "INTERNAL_SERVER_ERROR"));
    }
}
