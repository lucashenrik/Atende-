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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.lucas.demo.model.CaminhoInfo;
import com.lucas.demo.model.Item;
import com.lucas.demo.model.ItemXml;
import com.lucas.demo.model.PedidosContext;
import com.lucas.demo.model.Prefixo;

@Service
public class PedidoServico {

	@Lazy
	@Autowired
	private ArquivoService arquivoServ;

	@Autowired
	private PrefixosService prefixoServ;

	ObjectMapper mapper = new ObjectMapper();

	private static final Logger logger = LoggerFactory.getLogger(PedidoServico.class);

	private List<Map<String, String>> pedidoMemoria = Collections.synchronizedList(new ArrayList<>());
	protected List<Map<String, String>> pedidosVerficados = new ArrayList<>();
	private List<Map<String, String>> pedidosEntregues = new ArrayList<>();
	private List<Map<String, String>> pedidosCancelados = new ArrayList<>();
	// private List<String> prefixosComoString;
	private List<String> prefixos;

	/*
	 * @PostConstruct public void init() { // CaminhoInfo caminhoInfo =
	 * MudancaSO.separatorParaPrefixos(); // String caminho =
	 * caminhoInfo.getCaminhoArquivo(); List<Prefixo> prefixosP =
	 * prefixoServ.carregarPrefixos(); prefixosComoString =
	 * prefixosP.stream().map(Prefixo::getPrefixo).collect(Collectors.toList()); }
	 */

	public void carregarPrefixosString(String idCliente) {
		List<Prefixo> prefixosP = prefixoServ.carregarPrefixos(idCliente);
		prefixos = prefixosP.stream().map(Prefixo::getPrefixo).collect(Collectors.toList());
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

	// Recebe o json e processa
	public boolean processarItens(String json, String idCliente) {
		XmlMapper xmlMapper = new XmlMapper();
		boolean sucesso = false;

		try {
			// Converte o XML para uma árvore JSON para navegar nos nós
			JsonNode rootNode = xmlMapper.readTree(json);

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
						processarItemNode(itemNode, xmlMapper, itensProcessados, idCliente);
					}
				} else {
					processarItemNode(itemArrayNode, xmlMapper, itensProcessados, idCliente);
				}
			}

			// Exibe e escreve os itens processados
			for (ItemXml item : itensProcessados) {
				try {
					arquivoServ.escreverPedido(item, idCliente);
					sucesso = true;
				} catch (ErroArquivoException e) {
					System.err.println("Erro ao abrir ou escrever no arquivo: " + e.getMessage());
				} catch (Exception e) {
					System.err.println("Erro inesperado ao salvar o item: " + e.getMessage());
				}
			}
			return sucesso;
		} catch (IOException e) {
			throw new ErroProcessamentoException("Erro ao ler o XML", e);
		} catch (ErroProcessamentoException e) {
			throw e;
		} catch (Exception e) {
			throw new ErroProcessamentoException("Erro inesperado ao processar o XML", e);
		}
	}

	// Método auxiliar para processar um único itemNode
	private void processarItemNode(JsonNode itemNode, XmlMapper xmlMapper, List<ItemXml> itensProcessados,
			String idCliente) {
		try {
			// Verifica se os campos obrigatórios existem e são válidos
			if (itemNode.hasNonNull("id") && itemNode.hasNonNull("description") && itemNode.hasNonNull("quantity")) {

				// Converte o nó JSON em objeto ItemXml
				ItemXml novoItem = xmlMapper.treeToValue(itemNode, ItemXml.class);

				// Verifica se o novoItem não é nulo e se o nome segue um prefixo esperado
				if (novoItem != null && começaComPrefixo(novoItem.getName(), idCliente)) {
					processarItem(novoItem, itensProcessados, idCliente);
				} else {
					logger.info("Item ignorado nulo ou sem nenhum dos prefixos: " + itemNode.toString());
				}
			} else {
				logger.warn("Item malformado: {}", itemNode.toString());
			}
		} catch (Exception e) {
			logger.error("Erro ao processar item: {}", e.getMessage(), e);
		}
	}

	// Método auxiliar para processar os itens
	private void processarItem(ItemXml novoItem, List<ItemXml> itensProcessados, String idCliente) {
		if (novoItem != null && começaComPrefixo(novoItem.getName(), idCliente)) {
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
		System.out.println(item);
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

		} catch (ItemNaoEncontradoException e) {
			throw new ItemNaoEncontradoException("Não foi possivel adicionar item. ", e);
		}
	}

	public synchronized boolean carregarPedidos(String idCliente) {
		pedidosVerficados.clear();
		pedidosEntregues.clear();
		pedidoMemoria.clear();

		boolean sucesso = true;
		LocalDate dataAtual = LocalDate.now();
		LocalTime horaAtual = LocalTime.now();
		LocalDate dataAnterior = dataAtual.minusDays(1);
		String caminhoArquivoAtual = verificarHora(idCliente); // Pega o caminho do arquivo baseado na hora atual
		String caminhoArquivoAnterior = caminhoArquivo(dataAnterior, idCliente); // Define o caminho do dia anterior

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

	protected PedidosContext carregarPedidosDeArquivo(String caminhoArquivo) throws ErroArquivoException {
		PedidosContext pedidoContext = new PedidosContext();
		File arquivo = new File(caminhoArquivo);

		if (arquivo.exists()) {
			try {
				List<Map<String, String>> pedidosArquivo = mapper.readValue(arquivo,
						new TypeReference<List<Map<String, String>>>() {
						});

				for (Map<String, String> item : pedidosArquivo) {
					pedidoContext.getPedidosAll().add(item);
					
					String statusItem = item.get("status");

					if ("entregue".equals(statusItem)) {
						if (!pedidoContext.getPedidosEntregues().contains(item)) {
							pedidoContext.getPedidosEntregues().add(item);
						}
					} else if ("cancelar".equals(statusItem)) {
						if (!pedidoContext.getPedidosCancelados().contains(item)) {
							pedidoContext.getPedidosCancelados().add(item);
						}
					} else {
						if (!pedidoContext.getPedidosVerificados().contains(item)) {
							pedidoContext.getPedidosVerificados().add(item);
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
		return pedidoContext;
	}

	protected String caminhoArquivo(LocalDate data, String idCliente) {
		CaminhoInfo caminhoInfo = MudancaSO.separatorParaPedidos(idCliente);
		String caminhoArq = caminhoInfo.getCaminhoArquivo();

		return caminhoArq + data + ".json";
	}

	private String verificarHora(String idCliente) {
		CaminhoInfo caminhoInfo = MudancaSO.separatorParaPedidos(idCliente);
		String caminhoArq = caminhoInfo.getCaminhoArquivo();

		LocalDate hoje = LocalDate.now();
		LocalTime agora = LocalTime.now();
		LocalDate data;

		// Se a hora atual for antes das 7h, usar o dia anterior
		if (agora.isBefore(LocalTime.of(7, 0))) {
			data = hoje.minusDays(1); // Usa a data anterior
		} else {
			data = hoje; // Usa a data atual
		}
		String caminhoData = caminhoArq + data + ".json";

		return caminhoData;
	}

	public List<String> contar() {

		try {
			Map<String, Integer> contagemItems = new HashMap<>();

			List<String> listaContagem = new ArrayList<>();

			for (Map<String, String> item : pedidosVerficados) {

				if (item == null || item.isEmpty()) {
					continue;
				}

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

	public List<String> contar2(List<Map<String, String>> pedidos, String statusDesejado) {

		try {
			Map<String, Integer> contagemItems = new HashMap<>();
			List<String> listaContagem = new ArrayList<>();

			for (Map<String, String> item : pedidos) {

				if (item == null || item.isEmpty()) {
					continue;
				}

				String nomeItem = item.get("description");
				String primeiroNome = nomeItem.split(" ")[0];
				String status = item.get("status");

				if ((statusDesejado.equals(status) && nomeItem != null)) {
					String quantityString = item.get("quantity");
					int quantity = Integer.parseInt(quantityString);

					// Verifica se o item já foi contado, se sim, incrementa, senão adiciona
					contagemItems.put(primeiroNome, contagemItems.getOrDefault(primeiroNome, 0) + quantity);
				}
			}

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

	public List<Map<String, String>> getPedidosCancleados() {
		return pedidosCancelados;
	}

	private boolean começaComPrefixo(String descricao, String idCliente) {
		carregarPrefixosString(idCliente);
		return descricao != null && prefixos.stream().anyMatch(descricao::startsWith);
	}
}