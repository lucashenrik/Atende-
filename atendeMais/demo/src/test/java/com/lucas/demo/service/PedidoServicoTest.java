package com.lucas.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
/*
@ExtendWith(MockitoExtension.class)
public class PedidoServicoTest {

	@Mock
	private ArquivoService arquivoServ;
	@Mock
	private PrefixosService prefixoServ;
	
	private String idCliente = "teste@gmail.com";

	List<Prefixo> prefixosMockados = List.of(new Prefixo("Batata"), new Prefixo("Jantinha"));

	// @Autowired
	@InjectMocks
	private PedidoServico pedidoServico;

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	@DisplayName("Receber e processar apenas 1 item no xml com sucesso")
	public void processarItensCase1() {
		String xml = criarUmItem();

		Mockito.when(prefixoServ.carregarPrefixos(idCliente)).thenReturn(prefixosMockados);
		Boolean sucesso = pedidoServico.processarItens(xml, idCliente);

		assertThat(sucesso).isTrue();
	}

	@Test
	@DisplayName("Receber e processar 2 itens no xml com sucesso")
	public void processarItensCase2() {
		String xml = criarDoisItem();

		Mockito.when(prefixoServ.carregarPrefixos(idCliente)).thenReturn(prefixosMockados);
		Boolean sucesso = pedidoServico.processarItens(xml, idCliente);

		assertThat(sucesso).isTrue();
	}

	@Test
	@DisplayName("Receber e não processar item mal formado no xml")
	public void processarItensCase4() {
		String xml = criarItemMalFormado();

		boolean sucesso = pedidoServico.processarItens(xml, idCliente);

		assertThat(sucesso).isFalse();
	}

	@Test
	@DisplayName("Encontrar e buscar os pedidos no arquivo")
	public void carregarPedidos() {
		ResultadoCarregamentoPedidosDTO result = pedidoServico.carregarPedidos(idCliente);

		assertThat(result.isSucesso()).isTrue();
	}

	@Test
	@DisplayName("Não encontrar o arquivo com os pedidos (caminho inválido)")
	public void carregarPedidosCase2() throws ErroArquivoException {
		try {
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
			ResultadoCarregamentoPedidosDTO result = pedidoServico.carregarPedidos(idCliente);

			assertThat(result.isSucesso()).isTrue();

			// Verifica se o método carregarPedidosDeArquivo foi chamado
			Mockito.verify(spyPedidoServico, Mockito.atLeastOnce()).carregarPedidosDeArquivo(Mockito.anyString());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	@DisplayName("Deve retornar uma lista com a contagem correta dos pedidos")
	public void contarPedidosCase1() {
		try {
			List<String> resultado = this.mockarEContar();
			List<String> esperado = List.of("Batata: 5", "Jantinha: 7");

			assertThat(resultado).containsExactlyInAnyOrderElementsOf(esperado);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private List<String> mockarEContar() {
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

		PedidosContext mockPedidosContext = new PedidosContext();
		mockPedidosContext.getPedidosAll().add(pedido1);
		mockPedidosContext.getPedidosAll().add(pedido2);
		mockPedidosContext.getPedidosAll().add(pedido3);
		mockPedidosContext.getPedidosAll().add(pedido4);
		mockPedidosContext.getPedidosAll().add(pedido5);

		List<String> resultado = pedidoServico.contar(mockPedidosContext);

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
}*/