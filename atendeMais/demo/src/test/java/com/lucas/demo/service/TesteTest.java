package com.lucas.demo.service;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TesteTest {

	@Test
	@DisplayName("Teste simples do retorno falso usando JUnit")
	public void testarRetornoFalsoJUnit() {
	    boolean sucesso = false;
	    assertFalse(sucesso); // Deve passar
	}
}