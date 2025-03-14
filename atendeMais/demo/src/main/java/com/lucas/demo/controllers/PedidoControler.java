package com.lucas.demo.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.lucas.demo.exceptions.ErroProcessamentoException;
import com.lucas.demo.infra.security.AuthorizationSecurity;
import com.lucas.demo.model.PedidosContext;
import com.lucas.demo.model.dto.ResultadoCarregamentoPedidosDTO;
import com.lucas.demo.service.ArquivoService;
import com.lucas.demo.service.PedidoServico;

@ControllerAdvice
@RestController
@RequestMapping("/api/v1/pedido")
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
			pedidoServ.carregarPedidos(idCliente);

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
			ResultadoCarregamentoPedidosDTO result = pedidoServ.carregarPedidos(idCliente);
			PedidosContext pedidosContext = result.getPedidosContext();

			List<String> listaContagem = pedidoServ.contar(pedidosContext);
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
			ResultadoCarregamentoPedidosDTO result = pedidoServ.carregarPedidos(idCliente);
			PedidosContext pedidosContext = result.getPedidosContext();

			List<Map<String, String>> pedidosEntregue = pedidosContext.getPedidosEntregues();
			List<Map<String, String>> pedidosCancelados = pedidosContext.getPedidosCancelados();

			List<Map<String, String>> pedidosUnidos = new ArrayList<>();

			pedidosUnidos.addAll(pedidosEntregue);
			pedidosUnidos.addAll(pedidosCancelados);

			return ResponseEntity.ok(pedidosUnidos);
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
			ResultadoCarregamentoPedidosDTO result = pedidoServ.carregarPedidos(idCliente);
			PedidosContext pedidosContext = result.getPedidosContext();

			List<Map<String, String>> pedidos = pedidosContext.getPedidosVerificados();

			return ResponseEntity.ok(pedidos);
		} catch (Exception e) {
			throw e;
		}
	}

	// Retorna uma lista com pedidos prontos ou em produção
	@GetMapping("/{idEstabelecimento}/pedidos-clientes")
	public ResponseEntity<?> getPedidoClientes(@PathVariable String idEstabelecimento) {

		try {
			ResultadoCarregamentoPedidosDTO result = pedidoServ.carregarPedidos(idEstabelecimento);
			PedidosContext pedidosContext = result.getPedidosContext();

			List<Map<String, String>> pedidos = pedidosContext.getPedidosVerificados();

			return ResponseEntity.ok(pedidos);
		} catch (Exception e) {
			throw e;
		}
	}

	private void avisarFrontEnd() {
		messagingTemplate.convertAndSend("/topic/notifications", "Nova notificação recebida!");
	}
}