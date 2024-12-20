package com.lucas.demo.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lucas.demo.controllers.PedidoControler;
import com.lucas.demo.exceptions.ErroSocketException;
import com.lucas.demo.exceptions.ItemNaoEncontradoException;
import com.lucas.demo.model.Prefixo;
import com.lucas.demo.service.PrefixosService;

import jakarta.annotation.PostConstruct;

@Component
public class SocketServer {

	@Autowired
	private PrefixosService prefixoServ;

	@Autowired
	private PedidoControler pedidoControler;

	private final int port = 9100;

	// private static final Pattern CODE_PATTERN =
	// Pattern.compile("[A-Z]{2}-\\d{3}");

	private List<String> produtosEncontrados = new ArrayList<>();

	private List<String> prefixosString;

	// Mova o carregamento dos prefixos para o método @PostConstruct
	@PostConstruct
	public void init() {
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

						// Ler e imprimir todo o conteúdo do BufferedReader
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
		String senhaSemForma = null;
		String senhaFormatada = null;
		String lineCompleta;

		while ((line = reader.readLine()) != null) {
			// System.out.println("Dados brutos recebidos: " + line);

			if (line.contains("Senha")) {
				senhaSemForma = line;

				Pattern pattern = Pattern.compile("Senha do pedido : (\\d+)");
				Matcher matcher = pattern.matcher(senhaSemForma);

				if (matcher.find()) {
					senhaFormatada = matcher.group(1); // Captura apenas o número (exemplo: "117")
				}
			}

			// Limpeza da linha p remover códigos de escape ANSI e outros caracteres
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

		//System.out.println("Produtoo: " + produtosEncontrados + ", Código: " + senhaFormatada);

		if (produtosEncontrados != null && senhaSemForma != null) {
			criarXml(produtosEncontrados, senhaFormatada);
			produtosEncontrados.clear();
		} else {
			throw new ItemNaoEncontradoException("Produto ou código não encontrados no conteúdo recebido.");
		}
	}

	private boolean comecaComPrefixo(String descricao) {
		// Verifica se a descrição e a lista de prefixos estão corretas
		return descricao != null && prefixosString.stream().map(String::trim) // Remove espaços extras nos prefixos
				.anyMatch(prefixo -> descricao.trim().toUpperCase().startsWith(prefixo.toUpperCase())); // Compara
																										// ignorando
																										// maiúsculas/minúsculas
	}

	private String cleanLine(String line) {
		String cleanedLine = line.replaceAll("\u001B\\[[;\\d]*[A-Za-z]", ""); // Remove escape ANSI

		cleanedLine = cleanedLine.replaceAll("[\\x00-\\x1F\\x7F]", ""); // Remove caracteres de controle
		cleanedLine = cleanedLine.replaceAll("[^\\p{L}\\p{N}\\s]", ""); // Mantém apenas letras, números e espaços
		cleanedLine = cleanedLine.replaceAll("[^\\x20-\\x7E]", ""); // Remove caracteres não-imprimíveis (ASCII)
		cleanedLine = cleanedLine.trim();
		cleanedLine = cleanedLine.replaceFirst("^a0Et", ""); // Remove 'a0Et' do começo da linha, se existir

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

		pedidoControler.processarXml(xml);
	}

	//Teste de impressao
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
}