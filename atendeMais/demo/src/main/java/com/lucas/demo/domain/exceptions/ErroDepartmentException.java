package com.lucas.demo.domain.exceptions;

public class ErroDepartmentException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ErroDepartmentException(String message, Throwable cause) {
		super(message, cause);
	}

	public ErroDepartmentException(String message) {
		super(message);
	}

	@Override
	public String toString() {
		return "ErroArquivoException: " + getMessage() + " - Causa: " + getCause();
	}
}