package com.lucas.demo.domain.exceptions;

public class ErroRelatorioException extends RuntimeException {
	private static final long serialVersionUID = -5156008848585751317L;

	public ErroRelatorioException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public ErroRelatorioException(String message) {
		super(message);
	}
	
	@Override
	public String toString() {
		return "ErroRelatorioException: " + getMessage() + " - Causa: " + getCause();
	}
}
