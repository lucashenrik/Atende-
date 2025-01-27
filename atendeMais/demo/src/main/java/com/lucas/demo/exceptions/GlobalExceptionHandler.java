package com.lucas.demo.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ErroProcessamentoException.class)
	public ResponseEntity<String> handleErroProcesamentoJsonException(ErroProcessamentoException ex) {
		// Retorna uma resposta com o status HTTP 400 (Bad Request) e a mensagem da
		// exceção
		return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(ItemNaoEncontradoException.class)
	public ResponseEntity<String> handleItemNaoEncontradoException(ItemNaoEncontradoException ex) {
		return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(ErroArquivoException.class)
	public ResponseEntity<String> handleErroArquivoException(ErroArquivoException ex) {
		return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(ErroEscritaArquivoException.class)
	public ResponseEntity<String> handleErroEscritaArquivoException(ErroEscritaArquivoException ex) {
		return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(ErroLeituraArquivoException.class)
	public ResponseEntity<String> handleErroLeituaArquivoException(ErroLeituraArquivoException ex) {
		return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(ErroSocketException.class)
	public ResponseEntity<String> handleErroSocketException(ErroSocketException ex) {
		return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@ExceptionHandler(ErroRelatorioException.class)
	public ResponseEntity<String> handleErroRelatorioException(ErroRelatorioException ex){
		return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
	}
}