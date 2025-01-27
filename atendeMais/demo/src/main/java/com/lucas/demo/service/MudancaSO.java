package com.lucas.demo.service;

import java.io.File;

import com.lucas.demo.model.CaminhoInfo;

public class MudancaSO {

	public static CaminhoInfo separatorParaPedidos(String idCliente) {
		String diretorioAtual = System.getProperty("user.dir");
		File diretorioPrincipal = new File(diretorioAtual).getParentFile();
		if (File.separator.equals("\\")) {
			String diretorio = diretorioPrincipal + "\\clientes\\" + idCliente + "\\pedidos";
			String caminhoArq = diretorio + "\\pedidos_";
			
			return new CaminhoInfo(caminhoArq, diretorio);
			//return diretorioPrincipal + "\\clientes\\" + idCliente + "\\pedidos\\pedidos_";
		} else {
			String diretorio = diretorioPrincipal + "//atendeMais//clientes//" + idCliente + "//pedidos";
			String caminhoArq = diretorio + "//pedidos_";
			
			return new CaminhoInfo(caminhoArq, diretorio);
			//return "/home/atendeMais/registros/pedidos/pedidos_";
		}
	}

	public static CaminhoInfo separatorParaPrefixos(String idCliente) {
		String diretorioAtual = System.getProperty("user.dir");
		File diretorioPrincipal = new File(diretorioAtual).getParentFile();

		if (File.separator.equals("\\")) {
			String diretorio = diretorioPrincipal + "\\clientes" + "\\" + idCliente + "\\prefixos";
			String caminhoArq = diretorio + "\\prefixos_.json";
			return new CaminhoInfo(caminhoArq, diretorio);
		} else {
			String diretorio = diretorioPrincipal + "/atendeMais/Prefixos";
			String caminhoArq = diretorio + "/prefixos_.json";
			return new CaminhoInfo(caminhoArq, diretorio);
		}
	}

	public static CaminhoInfo separatorParaPrefixos() {
		String diretorioAtual = System.getProperty("user.dir");
		File diretorioPrincipal = new File(diretorioAtual).getParentFile();

		if (File.separator.equals("\\")) {
			String diretorio = diretorioPrincipal + "\\clientes" + "\\teste" + "\\prefixos";
			String caminhoArq = diretorio + "\\prefixos_.json";
			return new CaminhoInfo(caminhoArq, diretorio);
		} else {
			String diretorio = diretorioPrincipal + "/atendeMais/Prefixos";
			String caminhoArq = diretorio + "/prefixos_.json";
			return new CaminhoInfo(caminhoArq, diretorio);
		}
	}

	public static String separatorParaUserConfig() {
		String diretorioAtual = System.getProperty("user.dir");

		// Volte um nível removendo o último "demo" do caminho
		File diretorioPrincipal = new File(diretorioAtual).getParentFile();

		if (File.separator.equals("\\")) {
			return diretorioPrincipal + "\\user.json";
		} else {
			return diretorioPrincipal + "/atendeMais/user.json";
		}
	}

	public static String separatorParaAuth() {
		String diretorioAtual = System.getProperty("user.dir");

		// Volte um nível removendo o último "demo" do caminho
		File diretorioPrincipal = new File(diretorioAtual).getParentFile();

		if (File.separator.equals("\\")) {
			return "C:\\Users\\Lucas\\Documents\\Projetos\\demo\\user.json";
		} else {
			return diretorioPrincipal + "/atendeMais/user.json";
		}
	}
	
	public static CaminhoInfo separatorParaRelatorio(String idCliente) {
		String diretorioAtual = System.getProperty("user.dir");
		File diretorioPrincipal = new File(diretorioAtual).getParentFile();
		
		if (File.separator.equals("\\")) {
			String diretorio = diretorioPrincipal + "\\clientes\\" + idCliente + "\\relatorios";
			String caminhoArq = diretorio + "\\relatorio_";
			return new CaminhoInfo(caminhoArq, diretorio);
		} else {
			String diretorio = diretorioPrincipal + "//atendeMais//clientes" + idCliente + "//relatorios";
			String caminhoArq = diretorio + "//relatorio_";
			return new CaminhoInfo(caminhoArq, diretorio);
		}
	}
}