package com.example.expenseapp.exception;

import com.example.expenseapp.dto.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public MessageResponse handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return new MessageResponse(ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public MessageResponse handleInvalidCredentials(InvalidCredentialsException ex) {
        return new MessageResponse(ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public MessageResponse handleBadCredentials(BadCredentialsException ex) {
        return new MessageResponse("帳號或密碼錯誤");
    }

    @ExceptionHandler(AccountNotVerifiedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public MessageResponse handleAccountNotVerified(AccountNotVerifiedException ex) {
        return new MessageResponse(ex.getMessage());
    }

    @ExceptionHandler(AccountSuspendedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public MessageResponse handleAccountSuspended(AccountSuspendedException ex) {
        return new MessageResponse(ex.getMessage());
    }

    @ExceptionHandler(InvalidTokenException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public MessageResponse handleInvalidToken(InvalidTokenException ex) {
        return new MessageResponse(ex.getMessage());
    }

    @ExceptionHandler(TokenExpiredException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public MessageResponse handleTokenExpired(TokenExpiredException ex) {
        return new MessageResponse(ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public MessageResponse handleResourceNotFound(ResourceNotFoundException ex) {
        return new MessageResponse(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public MessageResponse handleGenericException(Exception ex) {
        return new MessageResponse("伺服器錯誤：" + ex.getMessage());
    }
}