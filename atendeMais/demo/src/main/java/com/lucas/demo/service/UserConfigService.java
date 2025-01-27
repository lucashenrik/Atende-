package com.lucas.demo.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class UserConfigService {

	@Autowired
	TokenService tokenService;

	String diretorioAtual = System.getProperty("user.dir");

	// Volte um nível removendo o último "demo" do caminho
	File diretorioPrincipal = new File(diretorioAtual).getParentFile();

	String caminho = diretorioPrincipal + "/atendeMais/user.json";
	// String caminho = diretorioPrincipal + "\\user.json";

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final Path userFilePath = Paths.get(caminho);

	public Map<String, String> getEmailAndToken() {
		try {
			JsonNode root = objectMapper.readTree(Files.newInputStream(userFilePath));
			JsonNode userNode = root.get("users").get(0); // Pega o primeiro e único usuário

			String email = userNode.get("email").asText();

			// Descriptografa o token
			String tokenDescripto = tokenDescripto();

			// Monta o mapa com o email e o token descriptografado
			Map<String, String> result = new HashMap<>();
			result.put("email", email);
			result.put("token", tokenDescripto);

			return result;
		} catch (IOException e) {
			throw new RuntimeException("Erro ao ler o arquivo user.json", e);
		} catch (Exception e) {
			throw new RuntimeException("Erro ao descriptografar o token", e);
		}
	}

	public void updateEmailAndToken(String email, String token) {
		try {
			// Encripta o token antes de atualizar
			String encryptedToken = tokenService.encryptToken(token);

			// Chama o método updateToken para atualizar o token encriptado
			updateToken(encryptedToken);

			// Atualiza o email no arquivo user.json
			JsonNode root = objectMapper.readTree(Files.newInputStream(userFilePath));
			ObjectNode userNode = (ObjectNode) root.get("users").get(0); // Pega o primeiro e único usuário
			userNode.put("email", email);

			// Escreve as alterações de volta ao arquivo
			objectMapper.writeValue(Files.newOutputStream(userFilePath), root);

		} catch (IOException e) {
			throw new RuntimeException("Erro ao atualizar o arquivo user.json", e);
		} catch (Exception e) {
			throw new RuntimeException("Erro ao encriptar o token", e);
		}
	}

	// Atualiza o token no arquivo user.json
	public void updateToken(String encryptedToken) throws IOException {
		File file = new File(caminho);

		Map<String, Object> data = objectMapper.readValue(file, new TypeReference<Map<String, Object>>() {
		});

		// Atualiza o token encriptado
		@SuppressWarnings("unchecked")
		Map<String, Object> userMap = (Map<String, Object>) ((List<Object>) data.get("users")).get(0);
		userMap.put("token", encryptedToken);

		// Grava de volta no arquivo
		objectMapper.writeValue(file, data);
	}

	// Retorna o token encriptado do arquivo user.json
	public String getToken() throws IOException {
		File file = new File(caminho);

		Map<String, Object> data = objectMapper.readValue(file, new TypeReference<Map<String, Object>>() {
		});

		// Retorna o token
		@SuppressWarnings("unchecked")
		Map<String, Object> userMap = (Map<String, Object>) ((List<Object>) data.get("users")).get(0);
		return (String) userMap.get("token");
	}

	private String tokenDescripto() throws Exception {
		String encryptedToken;
		String decryptedToken;
		try {
			encryptedToken = getToken(); // Recupera o token criptografado
			decryptedToken = tokenService.decryptToken(encryptedToken); // Descriptografa o token
			return decryptedToken; // Retorna o token descriptografado
		} catch (IOException e) {
			e.printStackTrace();
			throw new Exception("Erro ao descriptografar o token", e);
		}
	}
}