package com.lucas.demo.model;

public class CaminhoInfo {

	private final String caminhoArquivo;
	private final String diretorio;
	
	public CaminhoInfo(String caminhoArquivo, String diretorio) {
		this.caminhoArquivo = caminhoArquivo;
		this.diretorio = diretorio;
	}

	public String getCaminhoArquivo() {
		return caminhoArquivo;
	}

	public String getDiretorio() {
		return diretorio;
	}
}