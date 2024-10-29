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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lucas.demo.model.Prefixo;
import com.lucas.demo.service.AuthService;
import com.lucas.demo.service.PrefixosService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/prefixos")
public class PrefixosController {

	@Autowired
	private AuthService authService;
	
	@Autowired
	PrefixosService prefixoServ;

	@GetMapping("/buscar-prefixo")
	public ResponseEntity<?> buscarPrefixos(HttpSession session) {
		//if (authService.verificarSessao(session)) {
			// Carregar prefixos do arquivo
			List<Prefixo> prefixosCarregados = prefixoServ.carregarPrefixos(); // Chama o método que agora retorna a
																				// lista de prefixos

			if (prefixosCarregados.isEmpty()) {
				return ResponseEntity.noContent().build(); // Retorna 204 se a lista estiver vazia
			}
			return ResponseEntity.ok(prefixosCarregados);
		//}
		//return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	}

	@PostMapping("/adicionar-prefixo")
	public ResponseEntity<?> addPrefixo(@RequestBody Map<String, String> request, HttpSession session) {
		//if (authService.verificarSessao(session)) {
			String novoPrefixo = request.get("prefixo");
			prefixoServ.adicionarPrefixo(novoPrefixo);

			return new ResponseEntity<>(HttpStatus.CREATED);
		//}
		//return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	}

	@DeleteMapping("/excluir-prefixo")
	public ResponseEntity<?> excluirPrefixo(@RequestBody Prefixo prefixo, HttpSession session) {
		//if (authService.verificarSessao(session)) {
			// Carregar prefixos antes de tentar excluir
			prefixoServ.carregarPrefixos();

			// Excluir o prefixo
			prefixoServ.excluirPrefixo(prefixo.getPrefixo());

			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		//}
		//return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	}
}