package com.lucas.demo.infra.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationSecurity {

	@Autowired
	TokenService tokenServico;

	public String getToken(String authHeader) {
		String token = authHeader.replace("Bearer ", "");
		return token;
	}

	public String validarToken(String authHeader) {
		String token = this.getToken(authHeader);
		return this.getIdCliente(token);
	}

	public String getIdCliente(String token) {
		String idCliente = tokenServico.validateToken(token);
		return idCliente;
	}
}