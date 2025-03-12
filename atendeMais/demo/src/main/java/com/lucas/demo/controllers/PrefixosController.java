package com.lucas.demo.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.lucas.demo.infra.security.TokenServico;
import com.lucas.demo.model.Prefixo;
import com.lucas.demo.service.PrefixosService;

@RestController
@RequestMapping("/api/v1/prefixos")
public class PrefixosController {

	@Autowired
	private AuthorizationSecurity auth;

	@Autowired
	private PrefixosService prefixoServ;

	@Autowired
	private TokenServico tokenServico;

	@GetMapping("/buscar-prefixo")
	public ResponseEntity<?> buscarPrefixos(@RequestHeader("Authorization") String authHeader) {
		String token = auth.getToken(authHeader);
		String idCliente = auth.getIdCliente(token);
		
		if (idCliente != null) {
			// Carregar prefixos do arquivo
			List<Prefixo> prefixosCarregados = prefixoServ.carregarPrefixos(idCliente);

			if (prefixosCarregados.isEmpty()) {
				return ResponseEntity.noContent().build(); // Retorna 204 se a lista estiver vazia
			}
			return ResponseEntity.ok(prefixosCarregados);
		}
		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	}

	@PostMapping("/adicionar-prefixo")
	public ResponseEntity<?> addPrefixo(@RequestBody Map<String, String> request,
			@RequestHeader("Authorization") String authHeader) {
		String token = authHeader.replace("Bearer ", "");
		System.out.println("Token: " + token);
		String idCliente = tokenServico.validateToken(token);
		System.out.println("IdCliente: " + idCliente);

		if (idCliente != null) {
			String novoPrefixo = request.get("prefixo");
			prefixoServ.adicionarPrefixo(idCliente, novoPrefixo);

			return new ResponseEntity<>(HttpStatus.CREATED);
		}
		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	}

	@DeleteMapping("/excluir-prefixo")
	public ResponseEntity<?> excluirPrefixo(@RequestBody Prefixo prefixo,
			@RequestHeader("Authorization") String authHeader) {
		String token = auth.getToken(authHeader);
		String idCliente = auth.getIdCliente(token);
		System.out.println("IdClientess: " + idCliente);
		
		if (idCliente != null) {
			// Carregar prefixos antes de tentar excluir
			prefixoServ.carregarPrefixos(idCliente);
			prefixoServ.excluirPrefixo(prefixo.getPrefixo(), idCliente);

			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	}
}