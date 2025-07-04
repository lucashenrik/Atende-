package com.lucas.demo.domain.exceptions;

public class ErroEscritaArquivoException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ErroEscritaArquivoException(String message, Throwable cause) {
		super(message, cause);
	}

	public ErroEscritaArquivoException(String message) {
		super(message);
	}

	@Override
	public String toString() {
		return "ErroEscritaArquivoException: " + getMessage() + "- Causa: " + getCause();
	}
}