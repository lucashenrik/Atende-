package com.lucas.demo.domain.exceptions;

public class ErroUserException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ErroUserException(String message, Throwable cause) {
        super(message, cause);
    }

    public ErroUserException(String message) {
        super(message);
    }

    @Override
    public String toString() {
        return "ErroUserException: " + getMessage() + " - Causa: " + getCause();
    }
}