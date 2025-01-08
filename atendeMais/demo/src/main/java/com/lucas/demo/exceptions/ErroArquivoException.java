package com.lucas.demo.exceptions;

public class ErroArquivoException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ErroArquivoException(String message, Throwable cause) {
		super(message, cause);
	}

	public ErroArquivoException(String message) {
		super(message);
	}

	@Override
	public String toString() {
		return "ErroArquivoException: " + getMessage() + " - Causa: " + getCause();
	}
}