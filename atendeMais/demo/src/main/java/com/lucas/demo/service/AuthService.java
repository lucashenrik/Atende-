package com.lucas.demo.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucas.demo.model.User;
import com.lucas.demo.model.UserData;

import jakarta.servlet.http.HttpSession;

@Service
public class AuthService {

	// String diretorioAtual = System.getProperty("user.dir");

	// Volte um nível removendo o último "demo" do caminho
	// File diretorioPrincipal = new File(diretorioAtual).getParentFile();

	// String caminho = diretorioPrincipal + "/atendeMais/user.json";
	// String caminho = "C:\\Users\\Lucas\\Documents\\Projetos\\demo\\user.json";

	String caminho = MudancaSO.separatorParaAuth();

	public boolean autenticacao(String username, String password) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			UserData userData = objectMapper.readValue(new File(caminho), UserData.class);
			List<User> users = userData.getUsers();

			// Verifica se o username e a senha correspondem
			BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
			for (User user : users) {
				// System.out.println(user.getUsername() + " " + user.getPassword());
				// System.out.println(username + " " + password);

				// Compara a senha bruta com o hash armazenado
				if (user.getUsername().equals(username) && encoder.matches(password, user.getPassword())) {
					System.out.println("Login bem-sucedido!");
					return true; // Login bem-sucedido
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Login mal-sucedido!");
		return true;
		// return false; // Login falhou
	}

	public static String hashPassword(String password) {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		return encoder.encode(password);
	}

	public boolean verificarSessao(HttpSession session) {
		if (session.getAttribute("user") != null) {
			return true;
		}
		return true;
		// return false;
	}
}