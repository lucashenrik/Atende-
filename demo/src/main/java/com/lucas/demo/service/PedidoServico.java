package com.lucas.demo.service;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

//import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.lucas.demo.exceptions.ErroArquivoException;
import com.lucas.demo.exceptions.ErroProcessamentoException;
import com.lucas.demo.exceptions.ItemNaoEncontradoException;
import com.lucas.demo.model.Item;
import com.lucas.demo.model.ItemXml;
import com.lucas.demo.model.Prefixo;

import jakarta.annotation.PostConstruct;

@Service
public class PedidoServico {

	@Lazy
	@Autowired
	private ArquivoService arquivoServ;

	@Autowired
	private PrefixosService prefixoServ;

	@Autowired
	private UserConfigService userConfigService;

	ObjectMapper mapper = new ObjectMapper();

	private List<Map<String, String>> pedidoMemoria = Collections.synchronizedList(new ArrayList<>());

	// private List<Map<String, String>> pedidosEmArquivo = new ArrayList<>();

	private List<Map<String, String>> pedidosVerficados = new ArrayList<>();

	private List<Map<String, String>> pedidosEntregues = new ArrayList<>();

	private List<String> prefixosComoString;

	// Mova o carregamento dos prefixos para o método @PostConstruct
	@PostConstruct
	public void init() {
		List<Prefixo> prefixosP = prefixoServ.carregarPrefixos();
		prefixosComoString = prefixosP.stream().map(Prefixo::getPrefixo).collect(Collectors.toList());
	}

	public Item xmlParaPedido(String xml) throws JAXBException {
		// Cria o contexto JAXB para a classe Pedido
		JAXBContext context = JAXBContext.newInstance(Item.class);
		// Cria o Unmarshaller, que transforma o XML em objeto
		Unmarshaller unmarshaller = context.createUnmarshaller();
		// Converte o XML (em String) para um objeto Pedido
		StringReader reader = new StringReader(xml);
		Item item = (Item) unmarshaller.unmarshal(reader);
		return item;
	}

	// Retorna url para buscar informacoes do pedido
	public String getUrl(String noticacaoCode) {
		Map<String, String> userConfig = userConfigService.getEmailAndToken();
		String email = userConfig.get("email");
		String token = userConfig.get("token");

		String getUrl = "https://ws.pagseguro.uol.com.br/v3/transactions/notifications/" + noticacaoCode + "?email="
				+ email + "&token=" + token;

		return getUrl;
	}

	// Retorna a url para o servidor que processa o pedido
	public String urlProcess(String notificacaoCode) {
		String urlProcess = "http://localhost:8080/pedido/processar-notificacao?notificacaoCode=" + notificacaoCode;

		return urlProcess;
	}

	// Recebe o xml e processa
	public boolean processarItens(String xml) {
		// System.out.println(xml);
		XmlMapper xmlMapper = new XmlMapper();
		boolean sucesso = false;

		try {
			// Converte o XML para uma árvore JSON para navegar nos nós
			JsonNode rootNode = xmlMapper.readTree(xml);

			// Acessa todos os nós "items" dentro do nó raiz
			Iterable<JsonNode> itemsNodes = rootNode.findValues("items");

			// Verifica se encontrou nós "items"
			if (!itemsNodes.iterator().hasNext()) {
				throw new ErroProcessamentoException("O XML enviado não contém o campo 'items' ou está malformado.");
			}

			List<ItemXml> itensProcessados = new ArrayList<>();

			// Processa cada nó "items" encontrado
			for (JsonNode itemsNode : itemsNodes) {
				JsonNode itemArrayNode = itemsNode.path("item");

				// Verifica se 'item' é um array ou objeto único
				if (itemArrayNode.isArray()) {
					for (JsonNode itemNode : itemArrayNode) {
						processarItemNode(itemNode, xmlMapper, itensProcessados);
					}
				} else {
					processarItemNode(itemArrayNode, xmlMapper, itensProcessados);
				}
			}

			// Exibe e escreve os itens processados
			for (ItemXml item : itensProcessados) {
				try {
					sucesso = true;
					arquivoServ.escreverPedido(item); // Exemplo de escrita dos itens processados
				} catch (ErroArquivoException e) {
					System.err.println("Erro ao abrir ou escrever no arquivo: " + e.getMessage());
				} catch (Exception e) {
					System.err.println("Erro inesperado ao salvar o item: " + e.getMessage());
				}
			}
		} catch (IOException e) {
			throw new ErroProcessamentoException("Erro ao ler o XML", e);
		} catch (ErroProcessamentoException e) {
			throw e;
		} catch (Exception e) {
			throw new ErroProcessamentoException("Erro inesperado ao processar o XML", e);
		}
		return sucesso;
	}

	// Método auxiliar para processar um único itemNode
	private void processarItemNode(JsonNode itemNode, XmlMapper xmlMapper, List<ItemXml> itensProcessados) {
		try {
			// Verifica se os campos obrigatórios existem e são válidos
			if (itemNode.hasNonNull("id") && itemNode.hasNonNull("description") && itemNode.hasNonNull("quantity")) {
				// Converte o nó JSON em objeto ItemXml
				ItemXml novoItem = xmlMapper.treeToValue(itemNode, ItemXml.class);

				// Verifica se o novoItem não é nulo e se o nome segue um prefixo esperado
				if (novoItem != null && começaComPrefixo(novoItem.getName())) {
					processarItem(novoItem, itensProcessados);
				} else {
					// System.out.println("Item ignorado: " + itemNode.toString());
				}
			} else {
				System.out.println("Item malformado: " + itemNode.toString());
			}
		} catch (Exception e) {
			System.out.println("Erro ao processar item: " + e.getMessage());
		}
	}

	// Método auxiliar para processar os itens
	private void processarItem(ItemXml novoItem, List<ItemXml> itensProcessados) {
		if (novoItem != null && começaComPrefixo(novoItem.getName())) {
			ItemXml itemExistente = encontrarItemNaLista(itensProcessados, novoItem);

			if (itemExistente != null) {
				// Atualiza a quantidade se o item já existir
				itemExistente.setQuantity(itemExistente.getQuantity() + novoItem.getQuantity());
			} else {
				itensProcessados.add(novoItem);
			}
		}
	}

	// Método para encontrar um item com o mesmo nome e ID de referência na lista
	private ItemXml encontrarItemNaLista(List<ItemXml> items, ItemXml novoItem) {
		for (ItemXml item : items) {
			if (item.getName().equals(novoItem.getName())) {
				return item;
			}
		}
		return null;
	}

	public void adicionarItem(ItemXml item) {
		try {
			int id = item.getReferenceId();
			int quantity = item.getQuantity();

			String horaAtual = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

			String description = item.getName();

			Map<String, String> novoItem = new HashMap<>();
			novoItem.put("reference_id", String.valueOf(id));
			novoItem.put("quantity", String.valueOf(quantity));
			novoItem.put("description", description);
			novoItem.put("status", "andamento");
			novoItem.put("hora", horaAtual); // Adiciona a hora formatada

			// pedidoMemoria.add(novoItem);

		} catch (ItemNaoEncontradoException e) {
			throw new ItemNaoEncontradoException("Não foi possivel adicionar item. ", e);
		}
	}

	public synchronized boolean carregarPedidos() {
		pedidosVerficados.clear();
		pedidosEntregues.clear();
		pedidoMemoria.clear();

		boolean sucesso = true;
		LocalDate dataAtual = LocalDate.now();
		LocalTime horaAtual = LocalTime.now();
		LocalDate dataAnterior = dataAtual.minusDays(1);
		String caminhoArquivoAtual = verificarHora(); // Pega o caminho do arquivo baseado na hora atual
		String caminhoArquivoAnterior = caminhoArquivo(dataAnterior); // Define o caminho do dia anterior

		try {
			// Carregar pedidos do dia anterior somente se a hora for antes das 7h
			if (horaAtual.isBefore(LocalTime.of(7, 0))) {
				carregarPedidosDeArquivo(caminhoArquivoAnterior);
			}

			// Carregar pedidos do dia atual
			carregarPedidosDeArquivo(caminhoArquivoAtual);

		} catch (ErroArquivoException e) {
			sucesso = false;
			System.err.println("Erro ao carregar pedidos: " + e.getMessage());
		}

		return sucesso;
	}

	private void carregarPedidosDeArquivo(String caminhoArquivo) throws ErroArquivoException {
		File arquivo = new File(caminhoArquivo);

		if (arquivo.exists()) {
			try {
				List<Map<String, String>> pedidosArquivo = mapper.readValue(arquivo,
						new TypeReference<List<Map<String, String>>>() {
						});

				for (Map<String, String> item : pedidosArquivo) {
					String statusItem = item.get("status");

					if ("entregue".equals(statusItem) || "cancelar".equals(statusItem)) {
						if (!pedidosEntregues.contains(item)) {
							pedidosEntregues.add(item);
						}
					} else {
						if (!pedidosVerficados.contains(item)) {
							pedidosVerficados.add(item);
						}
						if (!pedidoMemoria.contains(item)) {
							pedidoMemoria.add(item);
						}
					}
				}

			} catch (IOException e) {
				throw new ErroArquivoException("Falha ao carregar pedidos do arquivo " + caminhoArquivo, e);
			}
		} else {
			throw new ErroArquivoException("Arquivo " + caminhoArquivo + " não encontrado.");
		}
	}

	private String caminhoArquivo(LocalDate data) {
		String diretorioAtual = System.getProperty("user.dir");
		File diretorioPrincipal = new File(diretorioAtual).getParentFile();
		// return diretorioPrincipal + "/atendeMais/registros/pedidos/pedidos_" + data +
		// ".json";

		return diretorioPrincipal + "\\registros\\pedidos\\pedidos_" + data + ".json";
	}

	private String verificarHora() {
		String diretorioAtual = System.getProperty("user.dir");

		File diretorioPrincipal = new File(diretorioAtual).getParentFile();
		// String caminhoAr = diretorioPrincipal +
		// "/atendeMais/registros/pedidos/pedidos_";

		String caminhoAr = diretorioPrincipal + "\\registros\\pedidos\\pedidos_";

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
		String caminhoArq = caminhoAr + data + ".json";
		// System.out.println("Caminho do arquivo: " + caminhoArq);

		return caminhoArq;
	}

	public List<String> contar() {

		try {
			Map<String, Integer> contagemItems = new HashMap<>();

			List<String> listaContagem = new ArrayList<>();

			// carregarPedidos();

			for (Map<String, String> item : pedidosVerficados) {

				String nomeItem = item.get("description");
				String primeiroNome = nomeItem.split(" ")[0];
				String status = item.get("status");

				if (("andamento".equals(status) && nomeItem != null)) {
					String quantityString = item.get("quantity");
					int quantity = Integer.parseInt(quantityString);

					// Verifica se o item já foi contado, se sim, incrementa, senão adiciona
					contagemItems.put(primeiroNome, contagemItems.getOrDefault(primeiroNome, 0) + quantity);
				}
			}

			// Exibe o resultado final no formato desejado
			for (Map.Entry<String, Integer> entry : contagemItems.entrySet()) {

				String nomeItem = entry.getKey();
				String primeiroNomeItem = nomeItem.split(" ")[0];
				int quantidade = entry.getValue();
				String result = primeiroNomeItem + ": " + quantidade;
				listaContagem.add(result);
			}

			return listaContagem;
		} catch (Exception e) {
			throw new ErroArquivoException("Erro inesperado ao tentar ler pedidos.", e.getCause());
		}
	}

	public List<Map<String, String>> getPedidoList() {
		return pedidosVerficados;
	}

	public List<Map<String, String>> getPedidosEntregues() {
		return pedidosEntregues;
	}

	private boolean começaComPrefixo(String descricao) {
		return descricao != null && prefixosComoString.stream().anyMatch(descricao::startsWith);
	}
}