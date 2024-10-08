package com.lucas.demo.controllers;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.lucas.demo.exceptions.ErroProcessamentoException;
import com.lucas.demo.model.Item;
import com.lucas.demo.service.ArquivoService;
import com.lucas.demo.service.PedidoServico;

@ControllerAdvice
@RestController
@RequestMapping("/pedido")
@CrossOrigin(origins = "http://localhost:3000")
//@CrossOrigin(origins = "http://192.168.1.5:3000")
public class PedidoControler {

	@Autowired
	PedidoServico pedidoServ;

	@Autowired
	ArquivoService arquivoServ;

	// private final List<SseEmitter> emitters = new ArrayList<>();
	List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

	RestTemplate restTemplate = new RestTemplate();

	@PostMapping("/notificationCode")
	public ResponseEntity<?> receiveNotification(@RequestParam("notificationCode") String notificacaoCode) {

		System.out.println("Received notificationCode: " + notificacaoCode);

		arquivoServ.escreverCodigo(notificacaoCode);

		String urlProcess = pedidoServ.urlProcess(notificacaoCode);

		try {
			// Realiza a requisicao GET
			ResponseEntity<String> response = restTemplate.getForEntity(urlProcess, String.class);
			return ResponseEntity.ok(response.getBody());
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar notificação.");
		}
	}

	@GetMapping("/processar-notificacao")
	public ResponseEntity<?> buscarPedido(@RequestParam("notificacaoCode") String notificacaoCode) {

		String url = pedidoServ.getUrl(notificacaoCode);

		try {
			// Realiza a requisicao GET
			ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
			String xmlResponse = response.getBody();

			Item item = pedidoServ.xmlParaPedido(xmlResponse);

			arquivoServ.escreverPedido(item);

			String message = "Salvo";
			return ResponseEntity.ok(message);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao consultar notificação.");
		}
	}

	/*
	 * @PostMapping("/alterar-status") public ResponseEntity<?>
	 * alterarStatus(@RequestBody String senha, String novoStatus){
	 * System.out.println("senha no controler: " + senha);
	 * arquivoServ.alterarStatus(senha, novoStatus);
	 * 
	 * return new ResponseEntity<>("Status alterado com sucesso",
	 * HttpStatus.CREATED); }
	 */

	@PostMapping("/alterar-status")
	public ResponseEntity<?> alterarStatusPedido(@RequestBody Map<String, String> payload) {
		String senha = payload.get("pedidoId");
		String novoStatus = payload.get("novoStatus");

		arquivoServ.alterarStatus(senha, novoStatus);

		return new ResponseEntity<>("Status alterado com sucesso", HttpStatus.CREATED);
	}

	@GetMapping("/contar")
	public ResponseEntity<?> contarPedidos() {
		List<String> listaContagem = pedidoServ.contar();
		return ResponseEntity.ok(listaContagem);
	}
	
	@GetMapping("/entregues")
	public ResponseEntity<?> getPedidosEntregues(){
		List<Map<String, String>> pedidosEntregue = pedidoServ.getPedidosEntregues();
		
		return ResponseEntity.ok(pedidosEntregue);
	}

	@PostMapping("/notificacoes")
	public ResponseEntity<?> processarNotificacoes(@RequestBody String json) throws ErroProcessamentoException {
		pedidoServ.processarItens(json);
		pedidoServ.getPedidoList();
		return ResponseEntity.ok("Sucesso!!");
	}

	@GetMapping("/lista-pedidos")
	public ResponseEntity<?> getLista() {
		pedidoServ.carregarPedidos();
		List<Map<String, String>> pedidos = pedidoServ.getPedidoList();
		// pedidos.forEach(System.out::println);

		return ResponseEntity.ok(pedidos);
	}
}
