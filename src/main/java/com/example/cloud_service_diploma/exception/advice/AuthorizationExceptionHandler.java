package com.example.cloud_service_diploma.exception.advice;

import com.example.cloud_service_diploma.exception.BadCredentials;
import com.example.cloud_service_diploma.model.Error;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

public class AuthorizationExceptionHandler {
    @ExceptionHandler(BadCredentials.class)
    public ResponseEntity<Error> handleBadCredentialsException(BadCredentials ex) {
        Error error = new Error("Bad credentials" + ex.getMessage(), 400);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}
