package com.lucas.demo.controllers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lucas.demo.infra.security.AuthorizationSecurity;
import com.lucas.demo.service.RelatorioService;

@RestController
@RequestMapping("/relatorio")
public class RelatorioController {

	@Autowired
	RelatorioService relatorioService;

	@Autowired
	AuthorizationSecurity auth;

	@GetMapping("/getContagem")
	public ResponseEntity<?> getContagem(@RequestHeader("Authorization") String authHeader,
			@RequestParam("dataString") String dataString) {
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate data = LocalDate.parse(dataString, formatter);
		System.out.println("Data: " + data);

		String token = auth.getToken(authHeader);
		String id = auth.getIdCliente(token);
		
		relatorioService.gerarPdf (data, id);		
		return ResponseEntity.ok("dd");
	}
}