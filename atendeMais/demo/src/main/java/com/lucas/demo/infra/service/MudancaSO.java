package com.lucas.demo.infra.service;

import java.io.File;

import com.lucas.demo.infra.context.CaminhoInfo;

public class MudancaSO {

	public static CaminhoInfo obterCaminhoPedidos(String estabelecimentoId) {
		String diretorioAtual = System.getProperty("user.dir");
		File diretorioPrincipal = new File(diretorioAtual).getParentFile();
		String diretorio;
		String caminhoArquivo;
		if (File.separator.equals("\\")) {
			diretorio = diretorioPrincipal + "\\clientes\\" + estabelecimentoId + "\\pedidos";
			caminhoArquivo = diretorio + "\\pedidos_";
		} else {
			diretorio = diretorioPrincipal + "/atendeMais/clientes/" + estabelecimentoId + "/pedidos";
			caminhoArquivo = diretorio + "/pedidos_";
		}
		return new CaminhoInfo(caminhoArquivo, diretorio);
	}

	public static CaminhoInfo obterCaminhoParaPrefixos(String estabelecimentoId) {
		String diretorioAtual = System.getProperty("user.dir");
		File diretorioPrincipal = new File(diretorioAtual).getParentFile();
		String diretorio;
		String caminhoArquivo;
		if (File.separator.equals("\\")) {
			diretorio = diretorioPrincipal + "\\clientes" + "\\" + estabelecimentoId + "\\prefixos";
			caminhoArquivo = diretorio + "\\prefixos_.json";
		} else {
			diretorio = diretorioPrincipal + "/atendeMais//clientes" + "/" + estabelecimentoId + "/prefixos";
			caminhoArquivo = diretorio + "/prefixos_.json";
		}
		return new CaminhoInfo(caminhoArquivo, diretorio);
	}

	public static CaminhoInfo obterCaminhoParaPrefixos() {
		String diretorioAtual = System.getProperty("user.dir");
		File diretorioPrincipal = new File(diretorioAtual).getParentFile();
		String diretorio;
		String caminhoArquivo;
		if (File.separator.equals("\\")) {
			diretorio = diretorioPrincipal + "\\clientes" + "\\teste" + "\\prefixos";
			caminhoArquivo = diretorio + "\\prefixos_.json";
			
		} else {
			diretorio = diretorioPrincipal + "/atendeMais/Prefixos";
			caminhoArquivo = diretorio + "/prefixos_.json";
		}
		return new CaminhoInfo(caminhoArquivo, diretorio);
	}
	
	public static CaminhoInfo obterCaminhoParaRelatorios(String estabelecimentoId) {
		String diretorioAtual = System.getProperty("user.dir");
		File diretorioPrincipal = new File(diretorioAtual).getParentFile();
		String diretorio;
		String caminhoArquivo;
		if (File.separator.equals("\\")) {
			diretorio = diretorioPrincipal + "\\clientes\\" + estabelecimentoId + "\\relatorios";
			caminhoArquivo = diretorio + "\\relatorio_";
		} else {
			diretorio = diretorioPrincipal + "/atendeMais/clientes" + estabelecimentoId + "/relatorios";
			 caminhoArquivo = diretorio + "/relatorio_";
		}
		return new CaminhoInfo(caminhoArquivo, diretorio);
	}
}