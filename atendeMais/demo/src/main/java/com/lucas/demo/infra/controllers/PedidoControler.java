package com.lucas.demo.infra.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.lucas.demo.application.PedidoUseCase;
import com.lucas.demo.infra.service.PedidoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lucas.demo.domain.exceptions.ErroProcessamentoException;
import com.lucas.demo.infra.security.AuthorizationSecurity;
import com.lucas.demo.infra.context.PedidosContext;
import com.lucas.demo.infra.context.ResultadoCarregamentoPedidosDTO;
import com.lucas.demo.infra.service.ArquivoService;

@RestController
@RequestMapping("/api/v1/pedido")
public class PedidoControler {

	private PedidoUseCase pedidoUseCase;
	private final ArquivoService arquivoService;
	private final SimpMessagingTemplate messagingTemplate;
	private final AuthorizationSecurity authorizationSecurity;

	public PedidoControler(PedidoUseCase pedidoUseCase, ArquivoService arquivoService,
						   SimpMessagingTemplate messagingTemplate, AuthorizationSecurity authorizationSecurity) {
		super();
		this.pedidoUseCase = pedidoUseCase;
		this.arquivoService = arquivoService;
		this.messagingTemplate = messagingTemplate;
		this.authorizationSecurity = authorizationSecurity;
	}

	// Processa a resposta xml
	@PostMapping("/notificacoes")
	public ResponseEntity<?> processarNotificacoes(@RequestBody String json,
			@RequestHeader("Authorization") String authHeader) throws ErroProcessamentoException {

		String estabelecimentoId = authorizationSecurity.validarToken(authHeader);
		if (estabelecimentoId == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		boolean processamentoBemSucedido = pedidoUseCase.newOrder(json, estabelecimentoId);
		if (processamentoBemSucedido) {
			pedidoUseCase.getOrders(estabelecimentoId);
			notificarFrontEnd();// Envie uma mensagem para o WebSocket
			return new ResponseEntity<>(HttpStatus.CREATED);
		}
		return new ResponseEntity<>("Erro desconhecido durante o processamento.", HttpStatus.INTERNAL_SERVER_ERROR);
	}

	// Altera o status de um item
	@PostMapping("/alterar-status")
	public ResponseEntity<?> alterarStatusPedido(@RequestBody Map<String, String> payload,
			@RequestHeader("Authorization") String authHeader) {

		String estabelecimentoId = authorizationSecurity.validarToken(authHeader);
		if (estabelecimentoId == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		String pedidoId = payload.get("pedidoId");
		String novoStatus = payload.get("novoStatus");
		String hora = payload.get("hora");

		pedidoUseCase.updateStatusOrder(pedidoId, novoStatus, hora, estabelecimentoId);
		notificarFrontEnd();

		return new ResponseEntity<>(HttpStatus.OK);
	}

	// Retorna uma lista com a contagem de cada item
	@GetMapping("/contar")
	public ResponseEntity<?> contarPedidos(@RequestHeader("Authorization") String authHeader) {

		String estabelecimentoId = authorizationSecurity.validarToken(authHeader);
		if (estabelecimentoId == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		ResultadoCarregamentoPedidosDTO resultado = pedidoUseCase.getOrders(estabelecimentoId);
		PedidosContext pedidosContext = resultado.getPedidosContext();
		List<String> contagemPedidos = pedidoUseCase.count(pedidosContext);

		return ResponseEntity.ok(contagemPedidos);
	}

	// Retorna uma lista apenas com pedidos entregues ou cancelados
	@GetMapping("/entregues")
	public ResponseEntity<?> getPedidosEntregues(@RequestHeader("Authorization") String authHeader) {

		String estabelecimentoId = authorizationSecurity.validarToken(authHeader);
		if (estabelecimentoId == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		ResultadoCarregamentoPedidosDTO resultado = pedidoUseCase.getOrders(estabelecimentoId);
		PedidosContext pedidosContext = resultado.getPedidosContext();
		List<Map<String, String>> pedidosEntregue = pedidosContext.getPedidosEntregues();
		List<Map<String, String>> pedidosCancelados = pedidosContext.getPedidosCancelados();

		List<Map<String, String>> pedidosCombinados = new ArrayList<>();
		pedidosCombinados.addAll(pedidosEntregue);
		pedidosCombinados.addAll(pedidosCancelados);

		return ResponseEntity.ok(pedidosCombinados);
	}

	// Retorna uma lista com pedidos prontos ou em produção
	@GetMapping("/lista-pedidos")
	public ResponseEntity<?> getLista(@RequestHeader("Authorization") String authHeader) {

		String estabelecimentoId = authorizationSecurity.validarToken(authHeader);
		if (estabelecimentoId == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		ResultadoCarregamentoPedidosDTO resultado = pedidoUseCase.getOrders(estabelecimentoId);
		PedidosContext pedidosContext = resultado.getPedidosContext();
		List<Map<String, String>> pedidosVerificados = pedidosContext.getPedidosVerificados();

		return ResponseEntity.ok(pedidosVerificados);
	}

	// Retorna uma lista com pedidos prontos ou em produção
	@GetMapping("/{idEstabelecimento}/pedidos-clientes")
	public ResponseEntity<?> getPedidoClientes(@PathVariable String idEstabelecimento,
			@RequestBody List<String> pedidoIds) {

		ResultadoCarregamentoPedidosDTO resultado = pedidoUseCase.getOrders(idEstabelecimento);
		PedidosContext pedidosContext = resultado.getPedidosContext();
		List<Map<String, String>> pedidosVerificados = pedidosContext.getPedidosVerificados();

		List<Map<String, String>> pedidoComId = pedidosVerificados.stream()
				.filter(p -> pedidoIds.contains(p.get("id"))
						&& (p.get("status").equals("andamento") || p.get("status").equals("pronto")))
				.collect(Collectors.toList());

		return ResponseEntity.ok(pedidoComId);
	}

	private void notificarFrontEnd() {
		messagingTemplate.convertAndSend("/topic/notifications", "Nova notificação recebida!");
	}
}