package com.lucas.demo.domain.exceptions;

public class ItemNaoEncontradoException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ItemNaoEncontradoException(String message, Throwable cause) {
		super(message, cause);
	}

	public ItemNaoEncontradoException(String message) {
		super(message);
	}

	@Override
	public String toString() {
		// Adicionando uma descrição detalhada para a exceção
		return "ItemNaoEncontradoException: " + getMessage() + " - Causa: " + getCause();
	}
}