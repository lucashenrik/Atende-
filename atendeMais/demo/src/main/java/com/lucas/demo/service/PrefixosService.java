package com.lucas.demo.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucas.demo.exceptions.ErroArquivoException;
import com.lucas.demo.exceptions.ErroEscritaArquivoException;
import com.lucas.demo.exceptions.ErroLeituraArquivoException;
import com.lucas.demo.model.CaminhoInfo;
import com.lucas.demo.model.Prefixo;

@Service
public class PrefixosService {

	private static final Logger logger = LoggerFactory.getLogger(PrefixosService.class);

	private ObjectMapper objectMapper = new ObjectMapper();
	private final List<String> prefixosCarregados = new ArrayList<>();

	String diretorioAtual = System.getProperty("user.dir");
	File diretorioPrincipal = new File(diretorioAtual).getParentFile();

	// private final String diretorio = diretorioPrincipal + "/atendeMais/Prefixos";
	// String caminhoArq = diretorio + "/prefixos_.json";

	// private final String diretorio = diretorioPrincipal + "\\Prefixos";
	// private final String caminhoArq = diretorio + "\\prefixos_.json";

	// private final String caminhoArq = diretorio + "\\prefixos_.json";

	CaminhoInfo caminhoInfo = MudancaSO.separatorParaPrefixos();

	private final String caminhoArq = caminhoInfo.getCaminhoArquivo();
	private final String diretorio = caminhoInfo.getDiretorio();

	public PrefixosService(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public PrefixosService() {
	}

	public boolean adicionarPrefixo(String prefixo) {

		boolean sucesso = false;
		prefixosCarregados.clear();

		List<Prefixo> prefixos = new ArrayList<>();

		validarDiretorio();

		File file = new File(caminhoArq);

		validarArquivo();

		// Carrega os prefixos existentes do arquivo
		if (file.exists()) {
			try {
				prefixos = objectMapper.readValue(file, new TypeReference<List<Prefixo>>() {
				});
				logger.debug("Prefixos existentes carregados: {}", prefixos);
			} catch (IOException e) {
				logger.error("Erro ao ler o arquivo: {}", e.getMessage(), e);
				throw new ErroLeituraArquivoException("Não foi possível ler o arquivo.", e);
			}
		}

		prefixos.add(new Prefixo(prefixo.trim()));

		// Escreve de volta no arquivo
		try {
			objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, prefixos);
			logger.info("Prefixo adicionado com sucesso: {}", prefixo);
			sucesso = true;
			return sucesso;
		} catch (IOException e) {
			logger.error("Não foi possível salvar o prefixo no arquivo: {}", e.getMessage(), e);
			throw new ErroEscritaArquivoException("Não foi possível salvar o prefixo no arquivo.", e);
		}
	}

	public List<Prefixo> carregarPrefixos() {
		prefixosCarregados.clear(); // Limpa a lista antes de adicionar novos
		logger.info("Iniciando carregamento de prefixos...");

		File file = new File(caminhoArq);
		List<Prefixo> prefixosList = new ArrayList<>();

		if (file.exists()) {
			try {
				prefixosList = objectMapper.readValue(file, new TypeReference<List<Prefixo>>() {
				});

				// Extrair os prefixos como uma lista de strings
				for (Prefixo p : prefixosList) {
					prefixosCarregados.add(p.getPrefixo());
				}
				logger.debug("Prefixos existentes carregados: {}", prefixosCarregados);
			} catch (IOException e) {
				logger.error("Erro ao ler o arquivo: {}", e.getMessage(), e);
				throw new ErroLeituraArquivoException("Falha ao carregar prefixos do arquivo.", e);
			}
		} else {
			System.err.println("Arquivo de prefixos não encontrado: " + caminhoArq);
			throw new ErroArquivoException("Arquivo de prefixos não encontrado.");
		}

		return prefixosList;
	}

	public boolean excluirPrefixo(String prefixo) {
		boolean sucesso = false;
		prefixosCarregados.clear();

		// Carregar prefixos do arquivo
		List<Prefixo> prefixosList = carregarPrefixos();

		boolean encontrado = false;

		// Usar um Iterator para remover o prefixo
		Iterator<Prefixo> iterator = prefixosList.iterator();
		while (iterator.hasNext()) {
			Prefixo prefixoTemp = iterator.next();
			if (prefixoTemp.getPrefixo().equals(prefixo)) {
				iterator.remove();
				encontrado = true;
				break;
			}
		}

		if (encontrado) {
			salvarPrefixosNoArquivo(prefixosList);
			logger.info("Prefixo removido com sucesso.");
			sucesso = true;
			return sucesso;
		} else {
			throw new ErroArquivoException("Prefixo não encontrado.");
		}

	}

	public void salvarPrefixosNoArquivo(List<Prefixo> prefixos) {
		try {
			objectMapper.writeValue(new File(caminhoArq), prefixos);
			logger.info("Prefixos salvos com sucesso no arquivo.");
		} catch (IOException e) {
			throw new ErroArquivoException("Não foi possível salvar os prefixos no arquivo.", e);
		}
	}

	protected void validarArquivo() {
		File file = new File(caminhoArq);
		if (!file.exists()) {
			try {
				// Tenta criar o arquivo, se necessário
				file.createNewFile(); // Usado para garantir que o arquivo existe
			} catch (IOException e) {
				throw new ErroArquivoException("Não foi possível criar o arquivo: " + caminhoArq, e);
			}
		}
	}

	protected void validarDiretorio() {
		File pasta = new File(diretorio);
		if (!pasta.exists() && !pasta.mkdirs()) {
			throw new ErroArquivoException("Não foi possível criar o diretório: " + diretorio);
		}
	}
}