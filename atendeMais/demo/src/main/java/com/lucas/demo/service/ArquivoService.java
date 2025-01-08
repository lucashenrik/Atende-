package com.lucas.demo.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucas.demo.exceptions.ErroLeituraArquivoException;
import com.lucas.demo.model.ItemXml;

@Service
public class ArquivoService {

	@Lazy
	@Autowired
	private PedidoServico pedidoServ;

	ObjectMapper mapper = new ObjectMapper();

	private List<Map<String, String>> pedidoList = new ArrayList<>();

	private String diretorioAtual = System.getProperty("user.dir");

	// String caminhoArq = diretorioPrincipal +
	// "/atendeMais/registros/pedidos/pedidos_" + data + ".json";
	// String caminhoArq = diretorioPrincipal +
	// "\\atendeMais\\registros\\pedidos\\pedidos_" + data + ".json";

	public void escreverCodigo(String notificationCode) {

		List<Map<String, String>> codigosList = new ArrayList<>();

		// Obtenha a data atual e a hora atual
		LocalDate hoje = LocalDate.now();
		LocalTime agora = LocalTime.now();
		LocalDate data;

		// Se a hora atual for antes das 7h, usar o dia anterior
		if (agora.isBefore(LocalTime.of(7, 0))) {
			data = hoje.minusDays(1); // Usa a data anterior
		} else {
			data = hoje; // Usa a data atual
		}

		String diretorio = "C:\\Users\\Lucas\\Documents\\Projetos\\demo\\Registros\\Codigos-de-notificacao";

		String caminhoArq = diretorio + "//registros//codigos-de-notificacao" + "\\notificacaoCode_" + data + ".json";

		// Cria o diretório se ele não existir
		File directory = new File(diretorio);
		if (!directory.exists()) {
			directory.mkdirs(); // Cria o diretório
		}

		// Verifica se o arquivo já existe e, se existir, carrega a lista de códigos
		// existente
		File file = new File(caminhoArq);
		if (file.exists()) {
			try {
				codigosList = mapper.readValue(file, new TypeReference<List<Map<String, String>>>() {
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Adiciona o novo código à lista
		Map<String, String> code = new HashMap<>();
		code.put("notificationCode", notificationCode);
		codigosList.add(code);

		escrever(codigosList, caminhoArq);
	}

	public synchronized void escreverPedido(ItemXml item) {
		verificarArquivo();

		String horaAtual = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

		Map<String, String> novoPedido = new HashMap<>();

		novoPedido = new HashMap<>();
		novoPedido.put("quantity", String.valueOf(item.getQuantity()));
		novoPedido.put("reference_id", String.valueOf(item.getReferenceId()));
		novoPedido.put("description", item.getName());
		novoPedido.put("status", "andamento");
		novoPedido.put("hora", horaAtual);

		pedidoList.add(novoPedido);

		// Escreve no arquivo após adicionar todos os pedidos
		escrever(pedidoList, verificarHora());
	}

	List<Map<String, Object>> pedidos = new ArrayList<>();

	public synchronized boolean alterarStatus(String senha, String novoStatus, String hora) {
		ObjectMapper objectMapper = new ObjectMapper();
		boolean sucesso = false;

		try {
			File file = recuperarListPedidos();
			// Atualiza o valor de pedidoEncontrado com o retorno do método
			// buscarAlterarStatusPedido
			boolean pedidoEncontrado = buscarAlterarStatusPedido(senha, novoStatus, hora);

			if (pedidoEncontrado) {
				try {
					// Escreve a lista de pedidos de volta no arquivo JSON
					objectMapper.writeValue(file, pedidos);

					System.out.println("Status atualizado com sucesso!");
					sucesso = true;

					pedidoServ.carregarPedidos(); // Recarregar pedidos para refletir as alterações
				} catch (IOException e) {
					throw new IOException("Pedido com a senha e hora especificados não encontrado.", e);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sucesso;
	}

	protected File recuperarListPedidos() {
		ObjectMapper objectMapper = new ObjectMapper();

		// Carrega o arquivo de pedidos para a lista
		File file = new File(verificarHora());

		if (file.exists()) {
			try {
				pedidos = objectMapper.readValue(file, new TypeReference<List<Map<String, Object>>>() {
				});
				return file;
			} catch (IOException e) {
				throw new ErroLeituraArquivoException("Não foi possivel ler o arquivo.", e.getCause());
			}
		}
		return file;
	}

	protected boolean buscarAlterarStatusPedido(String senha, String novoStatus, String hora) {
		boolean pedidoEncontrado = false;

		// Percorre os pedidos e altera o status do pedido com a senha e hora
		// especificadas
		for (Map<String, Object> pedidoNovo : pedidos) {
			if (pedidoNovo.containsKey("reference_id") && pedidoNovo.containsKey("hora")) {
				Object referenceId = pedidoNovo.get("reference_id");
				String referenceIdStr = String.valueOf(referenceId);
				String horaPedido = String.valueOf(pedidoNovo.get("hora"));

				if (referenceIdStr.equals(senha) && horaPedido.equals(hora)) {
					// Atualiza o status
					pedidoNovo.put("status", novoStatus);

					// Adiciona o horário apenas se o status for "cancelado" ou "entregue"
					if ("cancelar".equalsIgnoreCase(novoStatus) || "entregue".equalsIgnoreCase(novoStatus)) {

						String horaAtual = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

						pedidoNovo.put("hora", horaAtual);
					}
					pedidoEncontrado = true;
					break;
				}
			}
		}
		return pedidoEncontrado;
	}

	/*
	 * public synchronized boolean alterarStatus(String senha, String novoStatus,
	 * String hora) { ObjectMapper objectMapper = new ObjectMapper();
	 * 
	 * boolean sucesso = false; boolean pedidoEncontrado = false;
	 * 
	 * try { // Carrega o arquivo de pedidos para a lista File file = new
	 * File(verificarHora()); List<Map<String, Object>> pedidos = new ArrayList<>();
	 * // System.out.println("Tentando criar/escrever no arquivo: " + file);
	 * 
	 * if (file.exists()) { try { pedidos = objectMapper.readValue(file, new
	 * TypeReference<List<Map<String, Object>>>() { }); } catch (IOException e) {
	 * throw new ErroLeituraArquivoException("Não foi possivel ler o arquivo.",
	 * e.getCause()); } }
	 * 
	 * 
	 * // Percorre os pedidos e altera o status do pedido com a senha e hora //
	 * especificadas for (Map<String, Object> pedidoNovo : pedidos) { if
	 * (pedidoNovo.containsKey("reference_id") && pedidoNovo.containsKey("hora")) {
	 * Object referenceId = pedidoNovo.get("reference_id"); String referenceIdStr =
	 * String.valueOf(referenceId); String horaPedido =
	 * String.valueOf(pedidoNovo.get("hora"));
	 * 
	 * if (referenceIdStr.equals(senha) && horaPedido.equals(hora)) { // Atualiza o
	 * status pedidoNovo.put("status", novoStatus);
	 * 
	 * // Adiciona o horário apenas se o status for "cancelado" ou "entregue" if
	 * ("cancelar".equalsIgnoreCase(novoStatus) ||
	 * "entregue".equalsIgnoreCase(novoStatus)) {
	 * 
	 * String horaAtual =
	 * LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
	 * 
	 * pedidoNovo.put("hora", horaAtual); }
	 * 
	 * pedidoEncontrado = true; break; } } }
	 * 
	 * if (pedidoEncontrado) { try { // Escreve a lista de pedidos de volta no
	 * arquivo JSON objectMapper.writeValue(file, pedidos);
	 * 
	 * System.out.println("Status atualizado com sucesso!"); sucesso = true;
	 * 
	 * pedidoServ.carregarPedidos(); // Recarregar pedidos para refletir as
	 * alterações return sucesso; } catch (IOException e) { throw new
	 * IOException("Pedido com a senha e hora especificados não encontrado.", e); }
	 * }
	 * 
	 * } catch (IOException e) { e.printStackTrace(); } return sucesso; }
	 */

	protected String verificarHora() {
		File diretorioPrincipal = new File(diretorioAtual).getParentFile();

		LocalDate hoje = LocalDate.now();
		LocalTime agora = LocalTime.now();
		LocalDate data;

		// Se a hora atual for antes das 7h, usar o dia anterior
		if (agora.isBefore(LocalTime.of(7, 0))) {
			data = hoje.minusDays(1); // Usa a data anterior
		} else {
			data = hoje; // Usa a data atual
		}

		// Define o caminho do arquivo de forma limpa a cada chamada
		// String caminhoArq = diretorioPrincipal.getAbsolutePath() +
		// "/atendeMais/registros/pedidos/pedidos_" + data
		// + ".json";

		// String caminhoArq = diretorioPrincipal.getAbsolutePath() +
		// "\\registros\\pedidos\\pedidos_" + data + ".json";
		if (diretorioPrincipal == null || !diretorioPrincipal.exists()) {
			throw new IllegalStateException("Diretório principal inválido: " + diretorioAtual);
		}

		Path caminhoArq = Paths.get(diretorioPrincipal.getAbsolutePath(), "registros", "pedidos",
				"pedidos_" + data + ".json");

		Path caminhoReal = caminhoArq.toAbsolutePath().normalize();

		// System.out.println("Caminho do arquivo: " + caminhoArq);

		// return caminhoArq;

		// String caminhoArqui = caminhoReal.toString();
		// return caminhoArqui;

		return caminhoReal.toString();
	}

	protected List<Map<String, String>> verificarArquivo() {
		File file = new File(verificarHora());

		// Limpar a lista de pedidos sempre que começar um novo dia
		pedidoList = new ArrayList<>();

		// Verifica se o arquivo já existe e lê seu conteúdo
		if (file.exists()) {
			try {
				pedidoList = mapper.readValue(file, new TypeReference<List<Map<String, String>>>() {
				});
			} catch (IOException e) {
				throw new ErroLeituraArquivoException("Não foi possivel ler o arquivo.", e.getCause());
			}
		}
		return pedidoList;
	}

	protected synchronized void escrever(List<?> registro, String caminhoArq) {

		// Salva a lista atualizada de volta ao arquivo
		try (PrintWriter escrever = new PrintWriter(new FileWriter(caminhoArq))) {

			System.out.println("Escrevendo no arquivo: " + caminhoArq);

			// Converte a lista para JSON e escreve no arquivo
			String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(registro);
			// System.out.println("Escrevendo no arquivo: " + json); // Log para verificar o
			// conteúdo a ser escrito
			escrever.println(json);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}