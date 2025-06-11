package com.lucas.demo.infra.controllers;

import java.util.List;
import java.util.Map;

import com.lucas.demo.application.PrefixosUseCase;
import com.lucas.demo.infra.security.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
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
		this.auth = auth;
		this.prefixosUseCase = prefixosUseCase;
		this.tokenServico = tokenServico;
	}

	@GetMapping("/buscar-prefixo")
	public ResponseEntity<List<Prefixo>> buscarPrefixos() {
		String idCliente = this.getUsername();
		List<Prefixo> prefixosCarregados = prefixosUseCase.getAllPrefixos(idCliente);

		return ResponseEntity.ok(prefixosCarregados);
	}

	@PostMapping("/adicionar-prefixo")
	public ResponseEntity<Void> addPrefixo(@RequestBody Map<String, String> request) {
		String idCliente = this.getUsername();

		String novoPrefixo = request.get("prefixo");
		prefixosUseCase.createNewPrefixo(idCliente, novoPrefixo);

		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@DeleteMapping("/excluir-prefixo")
	public ResponseEntity<?> excluirPrefixo(@RequestBody Prefixo prefixo) {
		String idCliente = this.getUsername();
		prefixosUseCase.deletePrefixo(prefixo.getPrefixo(), idCliente);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	public String getUsername(){
		CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return userDetails.getUsername();
	}
}