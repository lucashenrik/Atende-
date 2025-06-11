package com.lucas.demo.infra.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lucas.demo.getway.ArquivoGetway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucas.demo.domain.exceptions.ErroArquivoException;
import com.lucas.demo.domain.exceptions.ErroLeituraArquivoException;
import com.lucas.demo.infra.context.CaminhoInfo;
import com.lucas.demo.infra.model.ItemXml;
import com.lucas.demo.infra.context.PedidosEFile;

@Service
public class ArquivoService implements ArquivoGetway {

	private ObjectMapper mapper = new ObjectMapper();
	private static final Logger logger = LoggerFactory.getLogger(ArquivoService.class);

	@Override
	public synchronized void newPedido(ItemXml item, String estabelecimentoId) {
		String horaAtual = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

		// Carrega ou inicializa a lista de pedidos a partir do arquivo
		List<Map<String, String>> pedidosExistentes = this.carregarPedidosDoArquivo(estabelecimentoId);

		Map<String, String> novoPedido = new HashMap<>();
		novoPedido.put("quantity", String.valueOf(item.getQuantity()));
		novoPedido.put("reference_id", String.valueOf(item.getReferenceId()));
		novoPedido.put("description", item.getName());
		novoPedido.put("status", "andamento");
		novoPedido.put("hora", horaAtual);

		pedidosExistentes.add(novoPedido);

		this.escrever(pedidosExistentes, this.obterCaminhoArquivo(estabelecimentoId)); // Escreve no arquivo após adicionar todos os
																	// pedidos
	}

	@Override
	public PedidosEFile getPedidos(String estabelecimentoId) {
		File file = new File(this.obterCaminhoArquivo(estabelecimentoId));
		List<Map<String, Object>> pedidos = new ArrayList<>();
		if (file.exists()) {
			try {
				pedidos = mapper.readValue(file, new TypeReference<List<Map<String, Object>>>() {
				});
			} catch (IOException e) {
				throw new ErroLeituraArquivoException("Não foi possivel ler o arquivo.", e);
			}
		}
		return new PedidosEFile(file, pedidos);
	}


	protected String obterCaminhoArquivo(String estabelecimentoId) {
		CaminhoInfo caminhoInfo = MudancaSO.obterCaminhoPedidos(estabelecimentoId);
		String caminhoBase = caminhoInfo.getCaminhoArquivo();
		File diretorio = new File(caminhoInfo.getDiretorio());

		LocalDate data = LocalTime.now().isBefore(LocalTime.of(7, 0)) ? LocalDate.now().minusDays(1) : LocalDate.now();

		if (!diretorio.exists() && !diretorio.mkdirs()) {
			logger.error("Diretório inválido: {}", diretorio);
			throw new ErroArquivoException("Diretório principal inválidoo: " + diretorio);
		}
		return caminhoBase + data + ".json";
	}

	protected List<Map<String, String>> carregarPedidosDoArquivo(String estabelecimentoId) {
		String caminho = this.obterCaminhoArquivo(estabelecimentoId);
		File file = new File(caminho);
		List<Map<String, String>> pedidoList = new ArrayList<>();

		// Verifica se o arquivo já existe e lê seu conteúdo
		if (file.exists()) {
			try {
				pedidoList = mapper.readValue(file, new TypeReference<List<Map<String, String>>>() {
				});
			} catch (IOException e) {
				throw new ErroArquivoException("Arquivo nao encontrado.", e.getCause());
			}
		}
		return pedidoList;
	}

	protected synchronized void escrever(List<?> registros, String caminhoArq) {
		// Salva a lista atualizada de volta ao arquivo
		try (PrintWriter writer = new PrintWriter(new FileWriter(caminhoArq))) {
			// Converte a lista para JSON e escreve no arquivo
			String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(registros);
			writer.println(json);
		} catch (IOException e) {
			logger.error("Erro ao escrever no arquivo {}: {}", caminhoArq, e.getMessage(), e);
		}
	}
}