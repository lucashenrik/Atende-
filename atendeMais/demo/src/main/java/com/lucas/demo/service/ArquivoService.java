package com.lucas.demo.service;

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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucas.demo.exceptions.ErroArquivoException;
import com.lucas.demo.exceptions.ErroLeituraArquivoException;
import com.lucas.demo.exceptions.ItemNaoEncontradoException;
import com.lucas.demo.model.CaminhoInfo;
import com.lucas.demo.model.ItemXml;

@Service
public class ArquivoService {

	@Lazy
	@Autowired
	private PedidoServico pedidoServ;

	ObjectMapper mapper = new ObjectMapper();
	private List<Map<String, String>> pedidoList = new ArrayList<>();

	public synchronized void escreverPedido(ItemXml item, String idCliente) {
		String horaAtual = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
		Map<String, String> novoPedido = new HashMap<>();

		verificarArquivo(idCliente);

		novoPedido = new HashMap<>();
		novoPedido.put("quantity", String.valueOf(item.getQuantity()));
		novoPedido.put("reference_id", String.valueOf(item.getReferenceId()));
		novoPedido.put("description", item.getName());
		novoPedido.put("status", "andamento");
		novoPedido.put("hora", horaAtual);

		pedidoList.add(novoPedido);

		// Escreve no arquivo após adicionar todos os pedidos
		escrever(pedidoList, verificarHora(idCliente));
	}

	List<Map<String, Object>> pedidos = new ArrayList<>();

	public synchronized boolean alterarStatus(String senha, String novoStatus, String hora, String idCliente) {
		ObjectMapper objectMapper = new ObjectMapper();
		boolean sucesso = false;

		try {
			File file = recuperarListPedidos(idCliente);
			boolean pedidoEncontrado = buscarAlterarStatusPedido(senha, novoStatus, hora);

			if (pedidoEncontrado) {
				try {
					objectMapper.writeValue(file, pedidos);

					sucesso = true;

					pedidoServ.carregarPedidos(idCliente); // Recarregar pedidos para refletir as alterações
				} catch (ItemNaoEncontradoException e) {
					throw new ItemNaoEncontradoException("Pedido com a senha e hora especificados não encontrado.", e);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sucesso;
	}

	protected File recuperarListPedidos(String idCliente) {
		ObjectMapper objectMapper = new ObjectMapper();
		File file = new File(verificarHora(idCliente));

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

	protected String verificarHora(String idCliente) {
		CaminhoInfo caminhoInfo = MudancaSO.separatorParaPedidos(idCliente);
		String caminhoArq = caminhoInfo.getCaminhoArquivo();
		File diretorio = new File(caminhoInfo.getDiretorio());

		LocalDate hoje = LocalDate.now();
		LocalTime agora = LocalTime.now();
		LocalDate data;

		// Se a hora atual for antes das 7h, usar o dia anterior
		if (agora.isBefore(LocalTime.of(7, 0))) {
			data = hoje.minusDays(1); // Usa a data anterior
		} else {
			data = hoje; // Usa a data atual
		}

		if (!diretorio.exists() && !diretorio.mkdirs()) {
			System.out.println("Diretorio: " + diretorio);
			throw new ErroArquivoException("Diretório principal inválidoo: " + diretorio);
		}

		String caminhoData = caminhoArq + data + ".json";

		return caminhoData;
	}

	protected List<Map<String, String>> verificarArquivo(String idCliente) {
		File file = new File(verificarHora(idCliente));

		// Limpar a lista de pedidos sempre que começar um novo dia
		pedidoList = new ArrayList<>();

		// Verifica se o arquivo já existe e lê seu conteúdo
		if (file.exists()) {
			System.out.println("file existe");
			try {
				pedidoList = mapper.readValue(file, new TypeReference<List<Map<String, String>>>() {
				});
			} catch (IOException e) {
				throw new ErroArquivoException("Arquivo nao encontrado.", e.getCause());
			}
		}
		return pedidoList;
	}

	protected synchronized void escrever(List<?> registro, String caminhoArq) {
		// Salva a lista atualizada de volta ao arquivo
		try (PrintWriter escrever = new PrintWriter(new FileWriter(caminhoArq))) {

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