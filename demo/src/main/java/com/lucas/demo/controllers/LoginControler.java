package com.lucas.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lucas.demo.service.AuthService;
import com.lucas.demo.service.PedidoServico;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/auth")
public class LoginControler {

	@Autowired
	private AuthService authService;
	
	@Autowired
	PedidoServico pedidoServ;
	
	@PostMapping("/login")
	public ResponseEntity<String> login(@RequestParam String username, @RequestParam String password, HttpSession session) {
		 
		if (authService.autenticacao(username, password)){
			session.setAttribute("user", username);
			return ResponseEntity.ok("Login sucesso!");
		}
		
		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	}
}