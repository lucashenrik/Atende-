package com.lucas.demo.controllers;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

	@GetMapping("/getRelatorio")
	public ResponseEntity<?> getContagem(@RequestHeader("Authorization") String authHeader,
			@RequestParam("dataString") String dataString) {
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate data = LocalDate.parse(dataString, formatter);

		String token = auth.getToken(authHeader);
		String id = auth.getIdCliente(token);
		
		File relatorio = relatorioService.gerarPdf (data, id);		
		Resource resource = new FileSystemResource(relatorio);
		
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_DISPOSITION, "attatchment; filename=\"" + relatorio.getName() + "\"");
	
		return ResponseEntity.ok()
				.headers(headers)
				.contentLength(relatorio.length())
				.contentType(MediaType.APPLICATION_PDF)
				.body(resource);
	}
}