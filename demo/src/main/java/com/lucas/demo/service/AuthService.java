package com.lucas.demo.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucas.demo.model.User;
import com.lucas.demo.model.UserData;

import infra.PasswordUtil;

@Service
public class AuthService {

	String caminho = "C:\\Users\\Lucas\\Documents\\Projetos\\demo\\user.json";
	// private static final String USERS_FILE = "user.json";

	/*public boolean autenticacao(String username, String password) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            UserData userData = objectMapper.readValue(new File(caminho), UserData.class);
            List<User> users = userData.getUsers();

            // Verifica se o username e a senha correspondem
            for (User user : users) {
            	System.out.println(user.getUsername() + user.getPassword());
            	System.out.println(username + password);
                if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                    return true; // Login bem-sucedido
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false; // Login falhou
    }*/
	
	public boolean autenticacao(String username, String password) {
	    try {
	        ObjectMapper objectMapper = new ObjectMapper();
	        UserData userData = objectMapper.readValue(new File(caminho), UserData.class);
	        List<User> users = userData.getUsers();

	        // Verifica se o username e a senha correspondem
	        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
	        for (User user : users) {
	            System.out.println(user.getUsername() + " " + user.getPassword());
	            System.out.println(username + " " + password);
	            
	            // Compara a senha bruta com o hash armazenado
	            if (user.getUsername().equals(username) && encoder.matches(password, user.getPassword())) {
	                return true; // Login bem-sucedido
	            }
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    return false; // Login falhou
	}

	
	 public static String hashPassword(String password) {
	        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
	        return encoder.encode(password);
	    }
	
}