package com.uber_clone.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

        // Spring przechwytuje wyjątek EmailAlreadyExistsException
        // i go obsługuje
        @ExceptionHandler(EmailAlreadyExistsException.class)
        public ResponseEntity<String> handleEmailAlreadyExists(
                        EmailAlreadyExistsException e) {
                return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(e.getMessage());
        }

        @ExceptionHandler(DriverAlreadyExistsException.class)
        public ResponseEntity<String> handleDriverAlreadyExists(
                        DriverAlreadyExistsException e) {
                return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(e.getMessage());
        }

        @ExceptionHandler(UserNotFoundException.class)
        public ResponseEntity<String> handleUserNotFound(
                        UserNotFoundException e) {
                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(e.getMessage());
        }

        @ExceptionHandler(UserIsNotDriverException.class)
        public ResponseEntity<String> handleUserIsNotDriver(UserIsNotDriverException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());

        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException e) {

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }

        @ExceptionHandler(RideNotFoundException.class)
        public ResponseEntity<String> handleRideNotFound(
                        RideNotFoundException e) {
                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(e.getMessage());
        }

}
