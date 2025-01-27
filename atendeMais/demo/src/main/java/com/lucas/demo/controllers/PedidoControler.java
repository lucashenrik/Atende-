package com.lucas.demo.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.lucas.demo.exceptions.ErroProcessamentoException;
import com.lucas.demo.infra.security.AuthorizationSecurity;
import com.lucas.demo.service.ArquivoService;
import com.lucas.demo.service.PedidoServico;

@ControllerAdvice
@RestController
@RequestMapping("/pedido")
public class PedidoControler {

	@Autowired
	private PedidoServico pedidoServ;

	@Autowired
	private ArquivoService arquivoServ;

	@Autowired
	private SimpMessagingTemplate messagingTemplate;

	@Autowired
	private AuthorizationSecurity auth;

	RestTemplate restTemplate = new RestTemplate();

	// Processa a resposta xml
	@PostMapping("/notificacoes")
	public ResponseEntity<?> processarNotificacoes(@RequestBody String json,
			@RequestHeader("Authorization") String authHeader) throws ErroProcessamentoException {
		String idCliente = auth.validarToken(authHeader);

		if (idCliente == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		boolean sucesso = pedidoServ.processarItens(json, idCliente);

		if (sucesso == true) {
			pedidoServ.getPedidoList();
			// Envie uma mensagem para o WebSocket
			avisarFrontEnd();
			return new ResponseEntity<>(HttpStatus.CREATED);
		}
		return new ResponseEntity<>("Erro desconhecido durante o processamento.", HttpStatus.INTERNAL_SERVER_ERROR);
	}

	// Altera o status de um item
	@PostMapping("/alterar-status")
	public ResponseEntity<?> alterarStatusPedido(@RequestBody Map<String, String> payload,
			@RequestHeader("Authorization") String authHeader) {
		String idCliente = auth.validarToken(authHeader);

		if (idCliente == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		try {
			String senha = payload.get("pedidoId");
			String novoStatus = payload.get("novoStatus");
			String hora = payload.get("hora");

			arquivoServ.alterarStatus(senha, novoStatus, hora, idCliente);
			avisarFrontEnd();

			return new ResponseEntity<>(HttpStatus.OK);
		} catch (Exception e) {
			throw e;
		}
	}

	// Retorna uma lista com a contagem de cada item
	@GetMapping("/contar")
	public ResponseEntity<?> contarPedidos(@RequestHeader("Authorization") String authHeader) {
		String idCliente = auth.validarToken(authHeader);

		if (idCliente == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		try {
			List<String> listaContagem = pedidoServ.contar();
			return ResponseEntity.ok(listaContagem);
		} catch (Exception e) {
			throw e;
		}
	}

	// Retorna uma lista apenas com pedidos entregues ou cancelados
	@GetMapping("/entregues")
	public ResponseEntity<?> getPedidosEntregues(@RequestHeader("Authorization") String authHeader) {
		String idCliente = auth.validarToken(authHeader);

		if (idCliente == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		try {
			List<Map<String, String>> pedidosEntregue = pedidoServ.getPedidosEntregues();
			return ResponseEntity.ok(pedidosEntregue);
		} catch (Exception e) {
			throw e;
		}
	}

	// Retorna uma lista com pedidos prontos ou em produção
	@GetMapping("/lista-pedidos")
	public ResponseEntity<?> getLista(@RequestHeader("Authorization") String authHeader) {
		String idCliente = auth.validarToken(authHeader);

		if (idCliente == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		
		try {
			pedidoServ.carregarPedidos(idCliente);
			List<Map<String, String>> pedidos = pedidoServ.getPedidoList();

			return ResponseEntity.ok(pedidos);
		} catch (Exception e) {
			throw e;
		}
	}

	private void avisarFrontEnd() {
		messagingTemplate.convertAndSend("/topic/notifications", "Nova notificação recebida!");
	}
}