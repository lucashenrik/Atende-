package com.lucas.demo.service;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

@Service
public class TokenService {

    private static final String ALGORITHM = "AES";
    private static final String SECRET_KEY = "MySecretKey12345"; // Sua chave de 16 caracteres

    // Encripta o token
    public String encryptToken(String token) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKey secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(token.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // Desencripta o token
    public String decryptToken(String encryptedToken) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKey secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedToken));
        return new String(decryptedBytes);
    }
}
