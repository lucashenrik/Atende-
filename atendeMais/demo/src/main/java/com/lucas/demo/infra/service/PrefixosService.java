package com.lucas.demo.infra.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucas.demo.domain.exceptions.ErroArquivoException;
import com.lucas.demo.domain.exceptions.ErroEscritaArquivoException;
import com.lucas.demo.domain.exceptions.ErroLeituraArquivoException;
import com.lucas.demo.getway.PrefixosGetWay;
import com.lucas.demo.infra.context.CaminhoInfo;
import com.lucas.demo.infra.model.Prefixo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PrefixosService implements PrefixosGetWay {
	private static final Logger logger = LoggerFactory.getLogger(com.lucas.demo.infra.service.PrefixosService.class);
	private ObjectMapper objectMapper = new ObjectMapper();

	public PrefixosService(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public PrefixosService() {
	}

	protected CaminhoInfo configurarCaminho(String estabelecimentoId) {
		return MudancaSO.obterCaminhoParaPrefixos(estabelecimentoId);
	}

	private CaminhoInfo configurarCaminhoPadrao() {
		return MudancaSO.obterCaminhoParaPrefixos();
	}

	@Override
	public boolean createNewPrefixo(String estabelecimentoId, String prefixo) {
		CaminhoInfo caminhoInfo = this.configurarCaminho(estabelecimentoId);
		this.validarDiretorio(caminhoInfo.getDiretorio());
		this.validarArquivo(caminhoInfo.getCaminhoArquivo());

		File file = new File(caminhoInfo.getCaminhoArquivo());
		List<Prefixo> prefixos = new ArrayList<>();
		try {
			if (file.exists()) { // Carrega os prefixos existentes do arquivo
				prefixos = objectMapper.readValue(file, new TypeReference<List<Prefixo>>() {
				});
				logger.debug("Prefixos existentes carregados: {}", prefixos);
			}
		} catch (IOException e) {
			logger.error("Erro ao ler o arquivo: {}", e.getMessage(), e);
			throw new ErroLeituraArquivoException("Não foi possível ler o arquivo.", e);
		}

		prefixos.add(new Prefixo(prefixo.trim()));

		try { // Escreve de volta no arquivo
			objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, prefixos);
			logger.info("Prefixo adicionado com sucesso: {}", prefixo);
			return true;
		} catch (IOException e) {
			logger.error("Não foi possível salvar o prefixo no arquivo: {}", e.getMessage(), e);
			throw new ErroEscritaArquivoException("Não foi possível salvar o prefixo no arquivo.", e);
		}
	}

	@Override
	public List<Prefixo> getAllPrefixos(String estabelecimentoId) {
		CaminhoInfo caminhoInfo = this.configurarCaminho(estabelecimentoId);

		File file = new File(caminhoInfo.getCaminhoArquivo());
		List<Prefixo> prefixosList = new ArrayList<>();

		if (file.exists()) {
			try {
				prefixosList = objectMapper.readValue(file, new TypeReference<List<Prefixo>>() {
				});
			} catch (IOException e) {
				logger.error("Erro ao ler o arquivo: {}", e.getMessage(), e);
				throw new ErroLeituraArquivoException("Falha ao carregar prefixos do arquivo.", e);
			}
		} else {
			logger.error("Arquivo de prefixos não encontrado: " + caminhoInfo.getCaminhoArquivo());
			throw new ErroArquivoException("Arquivo de prefixos não encontrado.");
		}
		return prefixosList;
	}

	@Override
	public List<Prefixo> getAllPrefixos() {
		logger.info("Iniciando carregamento de prefixos...");

		CaminhoInfo caminhoInfo = configurarCaminhoPadrao();
		File file = new File(caminhoInfo.getCaminhoArquivo());
		List<Prefixo> prefixosList = new ArrayList<>();

		if (file.exists()) {
			try {
				prefixosList = objectMapper.readValue(file, new TypeReference<List<Prefixo>>() {
				});
			} catch (IOException e) {
				logger.error("Erro ao ler o arquivo: {}", e.getMessage(), e);
				throw new ErroLeituraArquivoException("Falha ao carregar prefixos do arquivo.", e);
			}
		} else {
			logger.error("Arquivo de prefixos não encontrado: " + caminhoInfo.getCaminhoArquivo());
			throw new ErroArquivoException("Arquivo de prefixos não encontrado.");
		}
		return prefixosList;
	}

	@Override
	public boolean deletePrefixo(String prefixo, String estabelecimentoId) {
		CaminhoInfo caminhoInfo = this.configurarCaminho(estabelecimentoId);
		List<Prefixo> prefixosList = this.getAllPrefixos(estabelecimentoId); // Carregar prefixos do arquivo
		boolean removido = prefixosList.removeIf(p -> p.getPrefixo().equals(prefixo));

		if (removido) {
			salvarPrefixosNoArquivo(prefixosList, caminhoInfo.getCaminhoArquivo());
			logger.info("Prefixo removido com sucesso.");
			return true;
		} else {
			throw new ErroArquivoException("Prefixo não encontrado.");
		}
	}

	public void salvarPrefixosNoArquivo(List<Prefixo> prefixos, String caminho) {
		try {
			objectMapper.writeValue(new File(caminho), prefixos);
			logger.info("Prefixos salvos com sucesso no arquivo.");
		} catch (IOException e) {
			logger.error("Erro ao salvar os prefixos no arquivo: {}", e.getMessage(), e);
			throw new ErroArquivoException("Não foi possível salvar os prefixos no arquivo.", e);
		}
	}

	public void validarArquivo(String caminhoArq) {
		File file = new File(caminhoArq);
		if (!file.exists()) {
			try {
				if (file.createNewFile()) {
					try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
						writer.write("[]");
					}
				} else {
					logger.error("Falha ao criar o arquivo: {}", caminhoArq);
					throw new ErroArquivoException("Não foi possivel criar o arquivo: " + caminhoArq);
				}
			} catch (IOException e) {
				throw new ErroArquivoException("Não foi possível criar o arquivo: " + caminhoArq, e);
			}
		}
	}

	 public void validarDiretorio(String diretorio) {
		File pasta = new File(diretorio);
		if (!pasta.exists() && !pasta.mkdirs()) {
			logger.error("Não foi possível criar o diretório: {}", diretorio);
			throw new ErroArquivoException("Não foi possível criar o diretório: " + diretorio);
		}
	}
}