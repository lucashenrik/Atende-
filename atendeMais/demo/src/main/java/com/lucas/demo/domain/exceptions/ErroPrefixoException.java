package com.lucas.demo.domain.exceptions;

public class ErroPrefixoException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ErroPrefixoException(String message, Throwable cause) {
        super(message, cause);
    }

    public ErroPrefixoException(String message) {
        super(message);
    }

    @Override
    public String toString() {
        // Adicionando uma descrição detalhada para a exceção
        return "ErroProcessamentoException: " + getMessage() + " - Causa: " + getCause();
    }
}