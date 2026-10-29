package com.uber_clone.exceptions;

// Tworzenie unikalnych wyjątków pozwala obsłużyć konrketny wyjątek
// nie zmieniając obsługi innych błędów
public class EmailAlreadyExistsException extends RuntimeException {

    // Konstruktor
    public EmailAlreadyExistsException(String message) {
        super(message);
    }

}
