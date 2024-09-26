package com.lucas.demo.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lucas.demo.model.Prefixo;
import com.lucas.demo.service.PrefixosService;

@RestController
@RequestMapping("/prefixos")
public class PrefixosController {

	@Autowired
	PrefixosService prefixoServ;
	
	@GetMapping("/buscar-prefixo")
	public ResponseEntity<?> buscarPrefixos() {
	    // Carregar prefixos do arquivo
	    List<Prefixo> prefixosCarregados = prefixoServ.carregarPrefixos(); // Chama o método que agora retorna a lista de prefixos

	    if (prefixosCarregados.isEmpty()) {
	        return ResponseEntity.noContent().build(); // Retorna 204 se a lista estiver vazia
	    }

	    return ResponseEntity.ok(prefixosCarregados); // Retorna a lista de prefixos
	}

	@PostMapping("/adicionar-prefixo")
	public ResponseEntity<?> addPrefixo(@RequestBody Map<String, String> request) {
		String novoPrefixo = request.get("prefixo");
		prefixoServ.adicionarPrefixo(novoPrefixo);

		return ResponseEntity.ok("Prefixo adicionado com sucesso!!");
	}

	@PostMapping("/excluir-prefixo")
	public ResponseEntity<?> excluirPrefixo(@RequestBody Prefixo prefixo) {
		// Carregar prefixos antes de tentar excluir
		prefixoServ.carregarPrefixos();

		// Excluir o prefixo
		prefixoServ.excluirPrefixo(prefixo.getPrefixo());

		return ResponseEntity.ok("Prefixo excluído com sucesso.");
	}
}