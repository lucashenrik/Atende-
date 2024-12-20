package com.lucas.demo.controllers;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.lucas.demo.exceptions.ErroProcessamentoException;
import com.lucas.demo.model.ImageData;
import com.lucas.demo.service.ArquivoService;
import com.lucas.demo.service.AuthService;
import com.lucas.demo.service.PedidoServico;

import jakarta.servlet.http.HttpSession;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

@ControllerAdvice
@RestController
@RequestMapping("/pedido")
public class PedidoControler {

	@Autowired
	private AuthService authService;

	@Autowired
	private PedidoServico pedidoServ;

	@Autowired
	private ArquivoService arquivoServ;

	@Autowired
	private SimpMessagingTemplate messagingTemplate;

	RestTemplate restTemplate = new RestTemplate();

	// Recebe o Webhook e extrai o notificationCode
	@PostMapping("/notificationCode")
	public ResponseEntity<?> receiveNotification(@RequestBody String notificacaoCode) {

		try {
			// Decodifica a string codificada em URL
			String decodedNotification = URLDecoder.decode(notificacaoCode, StandardCharsets.UTF_8.name());

			// Extrai o valor de notificationCode do corpo da requisição
			String[] params = decodedNotification.split("&");
			String notificationCode = null;

			for (String param : params) {
				if (param.startsWith("notificationCode=")) {
					notificationCode = param.split("=")[1]; // Pega o valor após o "notificationCode="
					break;
				}
			}

			if (notificationCode == null) {
				return ResponseEntity.badRequest().body("notificationCode não encontrado.");
			}

			// Cria a url para fazer a requisição com o notificationCode
			String urlProcess = pedidoServ.getUrl(notificationCode);

			// Realiza a requisição GET com tratamento de exceções HTTP
			try {
				ResponseEntity<String> response = restTemplate.getForEntity(urlProcess, String.class);

				return processarNotificacoes(response.getBody());

			} catch (HttpClientErrorException e) {
				// Captura erros de cliente (4xx) como o 401 Unauthorized
				if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
					return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
							.body("Erro de autenticação: email ou token incorretos.");
				} else if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
					return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Recurso não encontrado.");
				} else {
					return ResponseEntity.status(e.getStatusCode()).body("Erro de cliente: " + e.getMessage());
				}
			} catch (HttpServerErrorException e) {
				// Captura erros de servidor (5xx)
				return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
						.body("Erro no servidor PagBank: " + e.getMessage());
			}

		} catch (Exception e) {
			// Captura qualquer outra exceção que não seja de HTTP
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar notificação.");
		}
	}

	// Processa a resposta xml
	@PostMapping("/notificacoes")
	public ResponseEntity<?> processarNotificacoes(@RequestBody String json) throws ErroProcessamentoException {
		// System.out.println(json);
		boolean sucesso = pedidoServ.processarItens(json);

		if (sucesso == true) {
			pedidoServ.getPedidoList();

			// Envie uma mensagem para o WebSocket
			avisarFrontEnd();

			return new ResponseEntity<>(HttpStatus.CREATED);
		}

		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	public ResponseEntity<?> processarXml(String xml) throws ErroProcessamentoException {
		boolean sucesso = pedidoServ.processarItens(xml);

		if (sucesso == true) {
			pedidoServ.getPedidoList();

			// Envie uma mensagem para o WebSocket
			avisarFrontEnd();

			return new ResponseEntity<>(HttpStatus.CREATED);
		}

		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	// Altera o status de um item
	@PostMapping("/alterar-status")
	public ResponseEntity<?> alterarStatusPedido(@RequestBody Map<String, String> payload, HttpSession session) {
		if (authService.verificarSessao(session)) {
			String senha = payload.get("pedidoId");
			String novoStatus = payload.get("novoStatus");
			String hora = payload.get("hora");

			arquivoServ.alterarStatus(senha, novoStatus, hora);

			avisarFrontEnd();

			return new ResponseEntity<>(HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	}

	// Retorna uma lista com a contagem de cada item
	@GetMapping("/contar")
	public ResponseEntity<?> contarPedidos(HttpSession session) {
		if (authService.verificarSessao(session)) {
			List<String> listaContagem = pedidoServ.contar();
			return ResponseEntity.ok(listaContagem);
		}
		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	}

	// Retorna uma lista apenas com pedidos entregues ou cancelados
	@GetMapping("/entregues")
	public ResponseEntity<?> getPedidosEntregues(HttpSession session) {

		if (authService.verificarSessao(session)) {
			List<Map<String, String>> pedidosEntregue = pedidoServ.getPedidosEntregues();
			return ResponseEntity.ok(pedidosEntregue);
		}
		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	}

	// Retorna uma lista com pedidos prontos ou em produção
	@GetMapping("/lista-pedidos")
	public ResponseEntity<?> getLista(HttpSession session) {

		if (authService.verificarSessao(session)) {
			pedidoServ.carregarPedidos();
			List<Map<String, String>> pedidos = pedidoServ.getPedidoList();

			return ResponseEntity.ok(pedidos);
		}
		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	}

	private void avisarFrontEnd() {
		messagingTemplate.convertAndSend("/topic/notifications", "Nova notificação recebida!");
	}

	// Scanner, sem utilidade
	@PostMapping("/orc")
	public String processImage(@RequestBody ImageData imageData) {
		// System.out.println("Iniciando OCR");
		String tessDataPath = "/usr/share/tesseract-ocr/4.00/tessdata"; // Caminho para tessdata

		try {
			// Decodificar a imagem Base64
			byte[] imageBytes = Base64.getDecoder().decode(imageData.getImage().split(",")[1]);
			BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));

			// Verifica se a imagem foi carregada corretamente
			if (img == null) {
				System.out.println("Erro: a imagem não foi carregada corretamente.");
				return "Erro ao processar imagem";
			}

			// Configuração do Tesseract
			Tesseract tesseract = new Tesseract();
			tesseract.setDatapath(tessDataPath); // Caminho do tessdata
			tesseract.setLanguage("eng"); // Definindo o idioma

			// Realizar OCR
			String result = tesseract.doOCR(img);
			System.out.println("Texto reconhecido: " + result);
			return result;

		} catch (TesseractException e) {
			System.err.println("Erro no Tesseract: " + e.getMessage());
			return "Erro no Tesseract ao processar a imagem";
		} catch (Exception e) {
			e.printStackTrace();
			return "Erro geral ao processar a imagem";
		}
	}
}