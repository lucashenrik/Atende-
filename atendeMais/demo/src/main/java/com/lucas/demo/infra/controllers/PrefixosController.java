package com.lucas.demo.infra.controllers;

import java.util.List;
import java.util.Map;

import com.lucas.demo.application.PrefixosUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lucas.demo.infra.security.AuthorizationSecurity;
import com.lucas.demo.infra.security.TokenService;
import com.lucas.demo.infra.model.Prefixo;

@RestController
@RequestMapping("/api/v1/prefixos")
public class PrefixosController {

	private final AuthorizationSecurity auth;
	private PrefixosUseCase prefixosUseCase;
	private final TokenService tokenServico;

	public PrefixosController(AuthorizationSecurity auth, PrefixosUseCase prefixosUseCase, TokenService tokenServico) {
		super();
		this.auth = auth;
		this.prefixosUseCase = prefixosUseCase;
		this.tokenServico = tokenServico;
	}

	@GetMapping("/buscar-prefixo")
	public ResponseEntity<List<Prefixo>> buscarPrefixos(@RequestHeader("Authorization") String authHeader) {
		String token = auth.getToken(authHeader);
		String idCliente = auth.getIdCliente(token);

		if (idCliente != null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		List<Prefixo> prefixosCarregados = prefixosUseCase.getAllPrefixos(idCliente);
		if (prefixosCarregados.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(prefixosCarregados);
	}

	@PostMapping("/adicionar-prefixo")
	public ResponseEntity<Void> addPrefixo(@RequestBody Map<String, String> request,
			@RequestHeader("Authorization") String authHeader) {
		String token = authHeader.replace("Bearer ", "");
		String idCliente = tokenServico.validateToken(token);

		if (idCliente != null) {
			String novoPrefixo = request.get("prefixo");
			prefixosUseCase.createNewPrefixo(idCliente, novoPrefixo);
			return ResponseEntity.status(HttpStatus.CREATED).build();
		}
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	}

	@DeleteMapping("/excluir-prefixo")
	public ResponseEntity<?> excluirPrefixo(@RequestBody Prefixo prefixo,
			@RequestHeader("Authorization") String authHeader) {
		String token = auth.getToken(authHeader);
		String idCliente = auth.getIdCliente(token);

		if (idCliente == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		prefixosUseCase.deletePrefixo(prefixo.getPrefixo(), idCliente);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
}