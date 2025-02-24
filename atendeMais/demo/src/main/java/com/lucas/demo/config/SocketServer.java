package com.lucas.demo.config;

//Devido ao uso da hospedagem esse funcionalidade foi deixada de lado.
//Essa abordagem foi pensada para funcionar em um ambiente local onde as maquininhas de pagamento
//se conectam ao sistema como se fosse uma terceira impressora.

/*@Component
public class SocketServer {

	@Autowired
	private PrefixosService prefixoServ;

	@Autowired
	private PedidoControler pedidoControler;

	private final int port = 9100;
	private static final Pattern CODE_PATTERN = Pattern.compile("[A-Z]{2}-(\\d{3})");
	private List<String> produtosEncontrados = new ArrayList<>();
	private List<String> prefixosString;

	s@PostConstruct
	public void init() {
		List<Prefixo> prefixosP = prefixoServ.carregarPrefixos();
		prefixosString = prefixosP.stream().map(Prefixo::getPrefixo).collect(Collectors.toList());
	}
	
	@PostConstruct
	public void init() {
		CaminhoInfo caminhoInfo = MudancaSO.separatorParaPrefixos();
		String caminho = caminhoInfo.getCaminhoArquivo();
		System.out.println("Caminho Arquivao: " + caminho);
		List<Prefixo> prefixosP = prefixoServ.carregarPrefixos();
		prefixosString = prefixosP.stream().map(Prefixo::getPrefixo).collect(Collectors.toList());
	}

	@PostConstruct
	public void startServer() {
		new Thread(() -> {
			try (ServerSocket serverSocket = new ServerSocket(port)) {
				System.out.println("Impressora escutando na porta " + port + "...");
				// sendDataToPrinter();

				while (true) {
					try (Socket clientSocket2 = serverSocket.accept();
							BufferedReader reader = new BufferedReader(
									new InputStreamReader(clientSocket2.getInputStream()))) {

						System.out.println("Conectado (BufferedReader) por: " + clientSocket2.getInetAddress());

						String line;
						StringBuilder receivedData = new StringBuilder();
						while ((line = reader.readLine()) != null) {
							receivedData.append(line).append("\n");
						}
						System.out.println("Dados recebidos:\n" + receivedData.toString());

						processData(new BufferedReader(new StringReader(receivedData.toString())));

					} catch (IOException e) {
						throw new ErroSocketException(
								"Erro ao processar a conexão do cliente com BufferedReader: " + e.getMessage());
					}
				}
			} catch (IOException e) {
				throw new ErroSocketException("Erro ao iniciar o servidor socket: " + e.getMessage());
			}
		}).start();
	}

	public void processData(BufferedReader reader) throws IOException {
		String line;
		String senhaFormatada = null;
		String lineCompleta;

		while ((line = reader.readLine()) != null) {
			// System.out.println("Dados brutos recebidos: " + line);

			if (senhaFormatada == null) {
				String senhaEncontrada = haSenha_PagBank(line);
				senhaFormatada = senhaEncontrada;
			}

			String cleanedLine = cleanLine(line);

			if (comecaComPrefixo(cleanedLine)) {
				String proxLine = reader.readLine();

				// Adiciona o produto à lista, combinando linhas se necessário
				if (proxLine != null && !proxLine.contains("$")) {
					lineCompleta = cleanedLine + " " + cleanLine(proxLine);
					produtosEncontrados.add(lineCompleta);
				} else {
					produtosEncontrados.add(cleanedLine);
				}
			}
		}

		// System.out.println("Produtoo: " + produtosEncontrados + ", Código: " +
		// senhaFormatada);

		if (produtosEncontrados != null && senhaFormatada != null) {
			criarXml(produtosEncontrados, senhaFormatada);
			produtosEncontrados.clear();
		} else {
			System.out.println("Produto ou código não encontrados no conteúdo recebido.");
		}
	}

	private String haSenha_PagBank(String line) {
		String senhaFormatada = null;
		Matcher matcher = CODE_PATTERN.matcher(line);

		if (matcher.find()) {
			senhaFormatada = matcher.group(1);
			System.out.println(senhaFormatada);
			return senhaFormatada;
		} else {
			return null;
		}
	}

	private String haSenha_Maquininha(String line) {
		String senhaFormatada = null;
		String senhaSemForma = null;

		if (line.contains("Senha")) {
			senhaSemForma = line;

			Pattern pattern = Pattern.compile("Senha do pedido : (\\d+)");
			Matcher matcher = pattern.matcher(senhaSemForma);

			if (matcher.find()) {
				senhaFormatada = matcher.group(1);
				System.out.println(senhaFormatada);
				return senhaFormatada;
			}
		}
		return null;
	}

	private boolean comecaComPrefixo(String descricao) {
		// Verifica se a descrição e a lista de prefixos estão corretas
		return descricao != null && prefixosString.stream().map(String::trim) // Remove espaços extras nos prefixos
				.anyMatch(prefixo -> descricao.trim().toUpperCase().startsWith(prefixo.toUpperCase())); // Compara
																										// ignorando
																										// maiúsculas/minúsculas
	}

	private String cleanLine(String line) {
		String cleanedLine = line.replaceAll("\u001B\\[[;\\d]*[A-Za-z]", "");

		cleanedLine = cleanedLine.replaceAll("[\\x00-\\x1F\\x7F]", "");
		cleanedLine = cleanedLine.replaceAll("[^\\p{L}\\p{N}\\s]", "");
		cleanedLine = cleanedLine.replaceAll("[^\\x20-\\x7E]", "");
		cleanedLine = cleanedLine.trim();
		cleanedLine = cleanedLine.replaceFirst("^a0Et", "");

		return cleanedLine;
	}

	private void criarXml(List<String> items, String senha) {
		StringBuilder xmlBuilder = new StringBuilder();

		xmlBuilder.append("<pedidos>\n");
		xmlBuilder.append("  <items>\n");

		for (String item : items) {
			xmlBuilder.append("    <item>\n");
			xmlBuilder.append("      <id>").append(senha).append("</id>\n");
			xmlBuilder.append("      <description>").append(item).append("</description>\n");
			xmlBuilder.append("      <quantity>").append(1).append("</quantity>\n");
			xmlBuilder.append("      <amount>").append(5.00).append("</amount>\n");
			xmlBuilder.append("    </item>\n");
		}

		xmlBuilder.append("  </items>\n");
		xmlBuilder.append("</pedidos>\n");

		String xml = xmlBuilder.toString();

		System.out.println(xml);

		//pedidoControler.processarXml(xml);
	}

	// Teste de impressao
	private final String data = "\u001Ba\u0001\u001BE\u0001\u001B!0\u001Bt(PagVendas\n\n"
			+ "\u001Ba\u0001\u001BE\u0001\u001B!0\u001Bt(UV-117\n\n"
			+ "\u001Ba\u0001\u001B!0\u001Bt(Senha do pedido : 117\n\n"
			+ "\u001BE \u001B! \u001Ba\u0001\u001Bt(------------------------------------------------\n"
			+ "\u001B! \u001BE \u001Bt(LUCAS HENRIK OLIVEIRA SILVA\n"
			+ "\u001Ba\u0001\u001Bt(------------------------------------------------\n\n"
			+ "\u001Ba\u0001\u001B!0\u001BE\u0001\u001Bt(BATATA ESPECIAL\n" + "\u001Ba\u0001\u001B! \u001Bt(R$15,00\n"
			+ "\u001BE \u001B!\n" + "\u001Bt(DINHEIRO\n" + "\u001Bt(22:27 | 08/11/2024\n" + "\u001B!\n"
			+ "\u001Ba\u0001\u001Bt(------------------------------------------------\n"
			+ "\u001Bt(CUPOM VALIDO SOMENTE\n" + "PARA ESTE EVENTO\n"
			+ "\u001Ba\u0001\u001Bt(------------------------------------------------\n"
			+ "\u001Ba\u0001\u001B!\u0001\u001Bt(Essa impressao nao e\n" + "responsabilidade do PagBank\n"
			+ "\u001Ba\u0001\u001BE\u0001\u001B!0\u001Bt(PagVendas\n\n"
			+ "\u001Ba\u0001\u001BE\u0001\u001B!0\u001Bt(UV-117\n\n"
			+ "\u001Ba\u0001\u001B!0\u001Bt(Senha do pedido : 117\n\n"
			+ "\u001BE \u001B! \u001Ba\u0001\u001Bt(------------------------------------------------\n"
			+ "\u001B! \u001BE \u001Bt(LUCAS HENRIK OLIVEIRA SILVA\n"
			+ "\u001Ba\u0001\u001Bt(------------------------------------------------\n\n"
			+ "\u001Ba\u0001\u001B!0\u001BE\u0001\u001Bt(BATATA ESPECIAL\n" + "\u001Ba\u0001\u001B! \u001Bt(R$15,00\n"
			+ "\u001BE \u001B!\n" + "\u001Bt(DINHEIRO\n" + "\u001Bt(22:27 | 08/11/2024\n" + "\u001B!\n"
			+ "\u001Ba\u0001\u001Bt(------------------------------------------------\n"
			+ "\u001Bt(CUPOM VALIDO SOMENTE\n" + "PARA ESTE EVENTO\n"
			+ "\u001Ba\u0001\u001Bt(------------------------------------------------\n"
			+ "\u001Ba\u0001\u001B!\u0001\u001Bt(Essa impressao nao e\n" + "responsabilidade do PagBank\n";

	@SuppressWarnings("unused")
	private void sendDataToPrinter() {
		try (Socket socket = new Socket("localhost", 9100); OutputStream outputStream = socket.getOutputStream()) {

			outputStream.write(data.getBytes("UTF-8"));
			outputStream.flush();
			System.out.println("Dados enviados para a impressora simulada na porta 9100.");

		} catch (Exception e) {
			System.err.println("Erro ao enviar dados para a porta 9100: " + e.getMessage());
		}
	}
}*/