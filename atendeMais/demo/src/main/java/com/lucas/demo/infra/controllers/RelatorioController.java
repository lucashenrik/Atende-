package com.lucas.demo.infra.controllers;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.lucas.demo.infra.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lucas.demo.infra.security.AuthorizationSecurity;
import com.lucas.demo.infra.service.RelatorioService;

@RestController
@RequestMapping("/api/v1/relatorio")
public class RelatorioController {

	RelatorioService relatorioService;

	public RelatorioController(RelatorioService relatorioService) {
		this.relatorioService = relatorioService;
	}

	@GetMapping("/getRelatorio")
	public ResponseEntity<Resource> getContagem(@RequestHeader("Authorization") String authHeader,
			@RequestParam("dataString") String dataString) {
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate data = LocalDate.parse(dataString, formatter);

		String estabelecimentoId = this.getUsername();
		
		File relatorio = relatorioService.gerarPdf (data, estabelecimentoId);		
		Resource resource = new FileSystemResource(relatorio);
		
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + relatorio.getName() + "\"");
	
		return ResponseEntity.ok()
				.headers(headers)
				.contentLength(relatorio.length())
				.contentType(MediaType.APPLICATION_PDF)
				.body(resource);
	}

	private String getUsername(){
		CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return userDetails.getUsername();
	}
}