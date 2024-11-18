package com.lucas.demo.exceptions;

public class ErroSocketException extends RuntimeException{

	private static final long serialVersionUID = 1L;

	public ErroSocketException (String message, Throwable cause) {
		super(message, cause);
	}
	
	public ErroSocketException (String message) {
		super(message);
	}
	
	@Override
	public String toString() {
		return "ErroSocketException:  " + getMessage() + " - Causa: " + getCause();
	}
}
