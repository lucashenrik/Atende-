package com.lucas.demo.model;

public class CaminhoInfo {

	private String caminhoArquivo;
	private String diretorio;

	public CaminhoInfo(String caminhoArquivo, String diretorio) {
		this.caminhoArquivo = caminhoArquivo;
		this.diretorio = diretorio;
	}

	public CaminhoInfo() {
	}

	public String getCaminhoArquivo() {
		return caminhoArquivo;
	}

	public String getDiretorio() {
		return diretorio;
	}
}