package com.lucas.demo.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucas.demo.model.Item;

@Service
public class ArquivoService {

	@Lazy
	@Autowired
	PedidoServico pedidoServ;

	ObjectMapper mapper = new ObjectMapper();

	public void escreverCodigo(String notificationCode) {

		ObjectMapper mapper = new ObjectMapper();

		List<Map<String, String>> codigosList = new ArrayList<>(); // Inicializa a lista vazia

		LocalDate data = LocalDate.now();

		// Especifica o caminho onde o arquivo será salvo
		String diretorio = "C:\\Users\\Lucas\\Documents\\Projetos\\demo\\Registros\\Codigos-de-notificacao";
		String caminhoArq = diretorio + "\\notificacaoCode_" + data + ".json";

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

	public void escreverPedido(Item item) {
		List<Map<String, String>> pedidoList = new ArrayList<>();

		LocalDate data = LocalDate.now();

		String diretorio = "C:\\Users\\Lucas\\Documents\\Projetos\\demo\\Registros\\Pedidos";
		String caminhoArq = diretorio + "\\pedidos_" + data + ".json";

		File file = new File(caminhoArq);

		// Verifica se o arquivo já existe e lê seu conteúdo
		if (file.exists()) {
			try {
				// Corrigido: lendo o arquivo JSON existente para a lista
				pedidoList = mapper.readValue(file, new TypeReference<List<Map<String, String>>>() {
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Inicialize novoPedido se ainda não estiver inicializado
		Map<String, String> novoPedido = new HashMap<>();

		// Obtenha a descrição do item, ou use o nome do item se a chave "description"
		// não existir
		/*
		 * String description = novoPedido.getOrDefault("description", item.getName());
		 * 
		 * List<String> prefixos = Arrays.asList("Jantinha", "Batata", "Porção",
		 * "Taboa");
		 * 
		 * boolean comecaComPrefixo = prefixos.stream()
		 * .anyMatch(description::startsWith);
		 * 
		 * // Compare a descrição com a string "Jantinha" usando equals if
		 * (comecaComPrefixo) { novoPedido.put("quantity",
		 * String.valueOf(item.getQuantity())); novoPedido.put("reference_id",
		 * String.valueOf(item.getReferenceId())); novoPedido.put("description",
		 * item.getName()); novoPedido.put("status", "andamento");
		 * 
		 * pedidoList.add(novoPedido); // Adiciona o novo item à lista
		 * 
		 * escrever(pedidoList, caminhoArq); // Adiciona o novo pedido e escreve no
		 * arquivo
		 * 
		 * pedidoServ.adicionarPedido(item);
		 * 
		 * }else { System.out.println("Nao eh uma comida"); }
		 */

		// Aqui, você deve adicionar o novo pedido à lista Map<String, String>
		novoPedido = new HashMap<>();
		novoPedido.put("quantity", String.valueOf(item.getQuantity()));
		novoPedido.put("reference_id", String.valueOf(item.getReferenceId()));
		novoPedido.put("description", item.getName());
		novoPedido.put("status", "andamento");

		pedidoList.add(novoPedido); // Adiciona o novo item à lista

		escrever(pedidoList, caminhoArq);

		pedidoServ.adicionarPedido(item);

		escrever(pedidoList, caminhoArq);

	}

	/*
	 * public void alterarStatus(String senha, String novoStatus) {
	 * 
	 * ObjectMapper objectMapper = new ObjectMapper();
	 * 
	 * try { List<Map<String, Object>> pedidos = new ArrayList<>();
	 * 
	 * LocalDate data = LocalDate.now();
	 * 
	 * String diretorio =
	 * "C:\\Users\\Lucas\\Documents\\Projetos\\demo\\Registros\\Pedidos"; String
	 * caminhoArq = diretorio + "\\pedidos_" + data + ".json";
	 * 
	 * File file = new File(caminhoArq);
	 * 
	 * for (Map<String, Object> pedidoNovo : pedidos) { if
	 * (pedidoNovo.get("reference_id").equals(senha)) { Item item =
	 * objectMapper.treeToValue(pedidoNovo, Item.class);
	 * 
	 * pedidoNovo.put("status", novoStatus);
	 * 
	 * break; } else {
	 * System.out.println("Pedido com a senha especificada não encontrado."); } }
	 * 
	 * pedidos.add(pedidoNovo); // Adiciona o novo item à lista
	 * 
	 * escrever(pedidos, caminhoArq);
	 * 
	 * pedidoServ.adicionarPedido(item);
	 * 
	 * escrever(pedidos, caminhoArq);
	 * 
	 * System.out.println("Status atualizado com sucesso!");
	 * 
	 * } catch (IOException e) { e.printStackTrace(); } }
	 */

	public void alterarStatus(String senha, String novoStatus) {
		ObjectMapper objectMapper = new ObjectMapper();

		try {
			// Local onde o arquivo está armazenado
			LocalDate data = LocalDate.now();
			String diretorio = "C:\\Users\\Lucas\\Documents\\Projetos\\demo\\Registros\\Pedidos";
			String caminhoArq = diretorio + "\\pedidos_" + data + ".json";

			// Carrega o arquivo de pedidos para a lista
			File file = new File(caminhoArq);
			List<Map<String, Object>> pedidos;

			if (file.exists()) {
				// Lê o arquivo JSON e converte para lista de pedidos
				pedidos = objectMapper.readValue(file, new TypeReference<List<Map<String, Object>>>() {
				});
			} else {
				System.out.println("Arquivo de pedidos não encontrado.");
				return; // Sai da função caso o arquivo não exista
			}

			// Variável de controle para saber se o pedido foi encontrado
			boolean pedidoEncontrado = false;
			System.out.println(senha);
			// Percorre os pedidos e altera o status do pedido com a senha especificada
			for (Map<String, Object> pedidoNovo : pedidos) {
				if (pedidoNovo.containsKey("reference_id")) {
					Object referenceId = pedidoNovo.get("reference_id");
					String referenceIdStr = String.valueOf(referenceId);

					System.out.println("Comparando reference_id: " + referenceIdStr + " com senha: " + senha);

					if (referenceIdStr.equals(senha)) {
						pedidoNovo.put("status", novoStatus); // Atualiza o status
						pedidoEncontrado = true;
						break;
					}
				}
			}
			if (!pedidoEncontrado) {
				System.out.println("Pedido com a senha especificada não encontrado.");
			} else {
				// Escreve a lista de pedidos de volta no arquivo JSON
				objectMapper.writeValue(file, pedidos);
				System.out.println("Status atualizado com sucesso!");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void escrever(List<?> registro, String caminhoArq) {
		// Salva a lista atualizada de volta ao arquivo
		try (PrintWriter escrever = new PrintWriter(new FileWriter(caminhoArq))) {
			// Converte a lista para JSON e escreve no arquivo
			String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(registro);
			// System.out.println("Escrevendo no arquivo: " + json); // Log para verificar o
			// conteúdo a ser escrito
			escrever.println(json); // Grava a lista inteira no arquivo
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}