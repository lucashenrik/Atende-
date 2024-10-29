package com.lucas.demo.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lucas.demo.service.AuthService;
import com.lucas.demo.service.UserConfigService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/user")
public class UserController {

	@Autowired
	private AuthService authService;

	@Autowired
	private UserConfigService userConfigService;

	// Endpoint para obter o email e o token
	@GetMapping("/get")
	public ResponseEntity<Map<String, String>> getEmailAndToken(HttpSession session) {
		if (authService.verificarSessao(session)) {
			Map<String, String> response = userConfigService.getEmailAndToken();
			return ResponseEntity.ok(response);
		}
		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	}

	// Endpoint para modificar o email e o token
	@PostMapping("/update")
	public ResponseEntity<String> updateEmailAndToken(@RequestBody Map<String, String> payload, HttpSession session) {
		if (authService.verificarSessao(session)) {
			String email = payload.get("email");
			String token = payload.get("token");
			try {

				// Salva o token encriptado no user.json
				userConfigService.updateEmailAndToken(email, token);
				// return ResponseEntity.ok("Token atualizado e encriptado com sucesso.");
				return ResponseEntity.ok("Email e Token atualizados com sucesso!");
			} catch (Exception e) {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao encriptar token.");
			}
		}
		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	}

	// Endpoint para atualizar o token encriptado
	/*
	 * @PostMapping("/updateToken") public ResponseEntity<String>
	 * updateToken(@RequestBody Map<String, String> request) { try { String token =
	 * request.get("token"); String encryptedToken =
	 * tokenService.encryptToken(token);
	 * 
	 * // Salva o token encriptado no user.json
	 * userConfigService.updateToken(encryptedToken); return
	 * ResponseEntity.ok("Token atualizado e encriptado com sucesso."); } catch
	 * (Exception e) { return
	 * ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).
	 * body("Erro ao encriptar token."); } }
	 * 
	 * // Endpoint para retornar o token desencriptado
	 * 
	 * @GetMapping("/getToken") public ResponseEntity<String> getToken() { try {
	 * String encryptedToken = userConfigService.getToken(); // Obter o token
	 * encriptado do arquivo String decryptedToken =
	 * tokenService.decryptToken(encryptedToken); return
	 * ResponseEntity.ok(decryptedToken); } catch (Exception e) { return
	 * ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).
	 * body("Erro ao desencriptar token."); } }
	 */
}