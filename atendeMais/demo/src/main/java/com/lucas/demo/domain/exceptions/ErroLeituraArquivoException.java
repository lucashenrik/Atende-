package com.lucas.demo.domain.exceptions;

public class ErroLeituraArquivoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ErroLeituraArquivoException(String message, Throwable cause) {
		super(message, cause);
	}

	public ErroLeituraArquivoException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return "ErroLeituraArquivoException: " + getMessage() + "- Causa: " + getCause();
	}

}