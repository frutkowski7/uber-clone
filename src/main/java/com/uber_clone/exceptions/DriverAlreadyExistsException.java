package com.uber_clone.exceptions;

public class DriverAlreadyExistsException extends RuntimeException {

    public DriverAlreadyExistsException(String message) {
        super(message);
    }

}
