package org.example.Exceptions;

public class NonExistantException extends RuntimeException {
    public NonExistantException(String message) {
        super(message);
    }
}
