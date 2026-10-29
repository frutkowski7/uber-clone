package com.uber_clone.exceptions;

public class UserIsNotDriverException extends RuntimeException {

    public UserIsNotDriverException(String message) {
        super(message);
    }

}
