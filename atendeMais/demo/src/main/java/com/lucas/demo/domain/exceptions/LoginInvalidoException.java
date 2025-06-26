package com.lucas.demo.domain.exceptions;

public class LoginInvalidoException extends RuntimeException {
    public LoginInvalidoException() {
        super("Email ou senha incorretos");
    }

    public LoginInvalidoException(String message) {
        super(message);
    }
}