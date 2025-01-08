package com.lucas.demo.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import com.lucas.demo.exceptions.ErroArquivoException;
import com.lucas.demo.model.Prefixo;

@ExtendWith(MockitoExtension.class)
public class PedidoServicoTest {

	@Mock
	private ArquivoService arquivoServ;

	@Mock
	private PrefixosService prefixoServ;

	@Mock
	private UserConfigService userConfigService;

	@Autowired
	@InjectMocks
	private PedidoServico pedidoServico;

	@BeforeEach
	void setup() {
		// Mocka os prefixos retornados
		List<Prefixo> prefixosMockados = List.of(new Prefixo("Batata"), new Prefixo("Jantinha"));
		Mockito.when(prefixoServ.carregarPrefixos()).thenReturn(prefixosMockados);

		// Inicializa a classe sob teste
		pedidoServico.init();
	}

	@Test
	@DisplayName("Receber e processar apenas 1 item no xml com sucesso")
	public void processarItensCase1() {
		String xml = criarUmItem();

		Boolean sucesso = pedidoServico.processarItens(xml);

		assertThat(sucesso).isTrue();
	}

	@Test
	@DisplayName("Receber e processar 2 itens no xml com sucesso")
	public void processarItensCase2() {
		String xml = criarDoisItem();
		Boolean sucesso = pedidoServico.processarItens(xml);

		assertThat(sucesso).isTrue();
	}

	@Test
	@DisplayName("Receber e não processar item mal formado no xml")
	public void processarItensCase3() {
		String xml = criarItemMalFormado();
		Boolean sucesso = pedidoServico.processarItens(xml);

		assertThat(sucesso).isFalse();
	}

	@Test
	@DisplayName("Encontrar e buscar os pedidos no arquivo")
	public void carregarPedidos() {
		Boolean sucesso = pedidoServico.carregarPedidos();

		assertThat(sucesso).isTrue();
	}

	@Test
	@DisplayName("Não encontrar o arquivo com os pedidos (caminho inválido)")
	public void carregarPedidosCase2() throws ErroArquivoException {

		// Cria um spy da classe PedidoServico para interceptar chamadas ao método
		// protegido
		PedidoServico spyPedidoServico = Mockito.spy(pedidoServico);

		// Simula o comportamento do método carregarPedidosDeArquivo com um caminho
		// inválido
		Mockito.doThrow(new ErroArquivoException("Arquivo invalido")).when(spyPedidoServico)
				.carregarPedidosDeArquivo(Mockito.anyString());

		// Substitui o pedidoServico pelo spy na injeção
		pedidoServico = spyPedidoServico;

		// Executa o método que será testado
		Boolean sucesso = pedidoServico.carregarPedidos();

		// Verifica se o retorno foi falsof
		assertThat(sucesso).isFalse();

		// Verifica se o método carregarPedidosDeArquivo foi chamado
		Mockito.verify(spyPedidoServico, Mockito.atLeastOnce()).carregarPedidosDeArquivo(Mockito.anyString());
	}

	@Test
	@DisplayName("Deve retornar uma lista com a contagem correta dos pedidos")
	public void contarPedidosCase1() {

		List<String> resultado = this.mockarEContar();

		List<String> esperado = List.of("Batata: 5", "Jantinha: 7");

		assertThat(resultado).containsExactlyInAnyOrderElementsOf(esperado);
	}

	private List<String> mockarEContar() {
		List<Map<String, String>> pedidosMockados = new ArrayList<>();

		Map<String, String> pedido1 = new HashMap<>();
		pedido1.put("reference_id", "1");
		pedido1.put("description", "Batata Especial");
		pedido1.put("quantity", "2");
		pedido1.put("status", "andamento");

		Map<String, String> pedido2 = new HashMap<>();
		pedido2.put("reference_id", "2");
		pedido2.put("description", "Batata");
		pedido2.put("quantity", "3");
		pedido2.put("status", "andamento");

		Map<String, String> pedido3 = new HashMap<>();
		pedido3.put("reference_id", "3");
		pedido3.put("description", "Jantinha Sa");
		pedido3.put("quantity", "3");
		pedido3.put("status", "andamento");

		Map<String, String> pedido4 = new HashMap<>();
		pedido4.put("reference_id", "4");
		pedido4.put("description", "Jantinha Sa");
		pedido4.put("quantity", "4");
		pedido4.put("status", "andamento");

		Map<String, String> pedido5 = new HashMap<>();
		pedido5.put("reference_id", "4");
		pedido5.put("description", "Jantinha Sa");
		pedido5.put("quantity", "4");
		pedido5.put("status", "cancelado");

		pedidosMockados.add(pedido1);
		pedidosMockados.add(pedido2);
		pedidosMockados.add(pedido3);
		pedidosMockados.add(pedido4);
		pedidosMockados.add(pedido5);

		pedidoServico.pedidosVerficados = pedidosMockados;

		List<String> resultado = pedidoServico.contar();

		return resultado;
	}

	private String criarUmItem() {
		return "<pedidos>" + "    <items>" + "        <item>" + "            <id>4</id>"
				+ "            <description>Batata Teste</description>" + "            <quantity>4</quantity>"
				+ "            <amount>5.00</amount>" + "        </item>" + "    </items>" + "</pedidos>";
	}

	private String criarDoisItem() {
		return "<pedidos>" + "    <items>" + "        <item>" + "            <id>4</id>"
				+ "            <description>Batata Teste</description>" + "            <quantity>4</quantity>"
				+ "            <amount>5.00</amount>" + "        </item>" + "    </items>" + "</pedidos>" + "<pedidos>"
				+ "    <items>" + "        <item>" + "            <id>4</id>"
				+ "            <description>Batata Teste 2</description>" + "            <quantity>1</quantity>"
				+ "            <amount>5.00</amount>" + "        </item>" + "    </items>" + "</pedidos>";
	}

	private String criarItemMalFormado() {
		return "<pedidos>" + "    <items>" + "        <item>" + "            <id>4</id>"
				+ "            <amount>5.00</amount>" + "        </item>" + "    </items>" + "</pedidos>";
	}
}