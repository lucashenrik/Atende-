package com.lucas.demo.domain.exceptions;

public class ErroProcessamentoException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ErroProcessamentoException(String message, Throwable cause) {
		super(message, cause);
	}

	public ErroProcessamentoException(String message) {
		super(message);
	}

	@Override
	public String toString() {
		// Adicionando uma descrição detalhada para a exceção
		return "ErroProcessamentoException: " + getMessage() + " - Causa: " + getCause();
	}
}