package com.lucas.demo.infra.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.lucas.demo.application.PedidoUseCase;
import com.lucas.demo.infra.model.dto.AlterarStatusDTO;
import com.lucas.demo.infra.security.CustomUserDetails;
import com.lucas.demo.infra.service.PedidoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
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
	private final SimpMessagingTemplate messagingTemplate;

	public PedidoControler(PedidoUseCase pedidoUseCase, SimpMessagingTemplate messagingTemplate) {
		this.pedidoUseCase = pedidoUseCase;
		this.messagingTemplate = messagingTemplate;
	}

	// Processa a resposta xml
	@PostMapping("/notificacoes")
	public ResponseEntity<?> processarNotificacoes(@RequestBody String json) throws ErroProcessamentoException {
		String estabelecimentoId = this.getUsername();

		boolean processamentoBemSucedido = pedidoUseCase.newOrder(json, estabelecimentoId);
		if (processamentoBemSucedido) {
			pedidoUseCase.getOrders(estabelecimentoId);
			notificarFrontEnd();// Envie uma mensagem para o WebSocket
		}

		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	// Altera o status de um item
	@PostMapping("/alterar-status")
	public ResponseEntity<?> alterarStatusPedido(@RequestBody AlterarStatusDTO payload) {
		String estabelecimentoId = this.getUsername();

		pedidoUseCase.updateStatusOrder(payload.pedidoId(), payload.novoStatus(), payload.hora(), estabelecimentoId);
		notificarFrontEnd();

		return new ResponseEntity<>(HttpStatus.OK);
	}

	// Retorna uma lista com a contagem de cada item
	@GetMapping("/contar")
	public ResponseEntity<?> contarPedidos() {
		String estabelecimentoId = this.getUsername();

		List<String> quantityOrders = pedidoUseCase.countOrders(estabelecimentoId);
		return ResponseEntity.ok(quantityOrders);
	}

	// Retorna uma lista apenas com pedidos entregues ou cancelados
	@GetMapping("/entregues")
	public ResponseEntity<?> getPedidosEntregues() {
		String estabelecimentoId = this.getUsername();
		List<Map<String, String>> ordersDelivered = pedidoUseCase.getOrdersDelivered(estabelecimentoId);

		return ResponseEntity.ok(ordersDelivered);
	}

	// Retorna uma lista com pedidos prontos ou em produção
	@GetMapping("/lista-pedidos")
	public ResponseEntity<?> getLista() {
		String estabelecimentoId = this.getUsername();
		List<Map<String, String>> ordersNoDelivered = pedidoUseCase.getOrdersNoDelivered(estabelecimentoId);

		return ResponseEntity.ok(ordersNoDelivered);
	}

	// Retorna uma lista com pedidos prontos ou em produção
	@GetMapping("/{idEstabelecimento}/pedidos-clientes")
	public ResponseEntity<?> getPedidoClientes(@PathVariable String idEstabelecimento,
			@RequestBody List<String> pedidoIds) {
		List<Map<String, String>> orderForClients = pedidoUseCase.getOrdersForClients(idEstabelecimento, pedidoIds);

		return ResponseEntity.ok(orderForClients);
	}

	private String getUsername(){
		CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return userDetails.getUsername();
	}

	private void notificarFrontEnd() {
		messagingTemplate.convertAndSend("/topic/notifications", "Nova notificação recebida!");
	}
}