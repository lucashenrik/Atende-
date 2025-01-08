package com.lucas.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.lucas.demo.model.Prefixo;

@ExtendWith(MockitoExtension.class)
public class PrefixosServiceTest {

	@Mock
	private ObjectMapper objectMapper;

	@Autowired
	@InjectMocks
	private PrefixosService prefixosServ;

	List<Prefixo> listaPrefixos = new ArrayList<>(Arrays.asList(new Prefixo("123"), new Prefixo("234")));

	List<Prefixo> listaPrefixosMock = new ArrayList<>(Arrays.asList(new Prefixo("Batata"), new Prefixo("Jantinha")));

	@SuppressWarnings("unchecked")
	@Test
	@DisplayName("Deve adicionar um novo prefixo")
	public void adicionarPrefixo() {

		try {
			when(objectMapper.readValue(any(File.class), any(TypeReference.class))).thenReturn(listaPrefixos);

			ObjectWriter writerMock = mock(ObjectWriter.class);

			when(objectMapper.writerWithDefaultPrettyPrinter()).thenReturn(writerMock);

			doNothing().when(writerMock).writeValue(any(File.class), any());

			boolean sucesso = prefixosServ.adicionarPrefixo("434");

			verify(objectMapper).readValue(any(File.class), any(TypeReference.class));
			verify(objectMapper.writerWithDefaultPrettyPrinter()).writeValue(any(File.class), any());

			assertTrue(sucesso);
		} catch (Exception e) {
			System.out.println("Exceção capturada: " + e.getMessage());
			fail("Teste falhou");
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	@DisplayName("Deve carregar os prefixos do arquivo.")
	public void carregarPrefixos() {
		try {
			List<Prefixo> listaPrefixosMock = Arrays.asList(new Prefixo("Batata"), new Prefixo("Jantinha"));

			when(objectMapper.readValue(any(File.class), any(TypeReference.class))).thenReturn(listaPrefixosMock);

			List<Prefixo> prefixosCarrregados = prefixosServ.carregarPrefixos();

			assertNotNull(prefixosCarrregados);
			assertEquals(2, prefixosCarrregados.size());
			assertThat(prefixosCarrregados).extracting(Prefixo::getPrefixo).contains("Batata", "Jantinha");

			verify(objectMapper).readValue(any(File.class), any(TypeReference.class));
		} catch (Exception e) {
			System.out.println("Exceção capturada: " + e.getMessage());
			e.printStackTrace();
			fail("Teste falhou");
		}
	}

	@Test
	@DisplayName("Deve excluir um prefixo")
	public void excluirPrefixo() {
		try {
			PrefixosService spyPrefixosServ = Mockito.spy(prefixosServ);

			doReturn(listaPrefixosMock).when(spyPrefixosServ).carregarPrefixos();

			doNothing().when(spyPrefixosServ).salvarPrefixosNoArquivo(anyList());

			boolean sucesso = spyPrefixosServ.excluirPrefixo("Batata");

			assertTrue(sucesso);
			assertThat(listaPrefixosMock).extracting(Prefixo::getPrefixo).doesNotContain("Batata");

			verify(spyPrefixosServ).carregarPrefixos();
			verify(spyPrefixosServ).salvarPrefixosNoArquivo(listaPrefixosMock);

		} catch (Exception e) {
			System.out.println("Exceção capturada: " + e.getMessage());
			fail("Exceção falhou");
		}
	}

	@Test
	@DisplayName("Deve salvar prefixos no arquivo.")
	public void salvarPrefixosNoArquivo() {
		try {
			ObjectMapper mockObjectMapper = mock(ObjectMapper.class);

			PrefixosService spyPrefixosService = Mockito.spy(PrefixosService.class);
			ReflectionTestUtils.setField(spyPrefixosService, "objectMapper", mockObjectMapper);

			spyPrefixosService.salvarPrefixosNoArquivo(listaPrefixosMock);

			verify(mockObjectMapper).writeValue(any(File.class), eq(listaPrefixosMock));
		} catch (Exception e) {
			System.out.println("Exceção capturada: " + e.getMessage());
			fail("Teste falhou");
		}
	}

	@Test
	@DisplayName("Deve criar o arquivo se ele não existir.")
	public void validarCriarArquivo() throws IOException {
		try {
			String caminhoAtual = System.getProperty("user.dir");
			String caminhoSup = new File(caminhoAtual).getParentFile().toString(); 
		
			String caminhoTeste = caminhoSup + "\\Prefixos\\teste.json";
			
			PrefixosService service = new PrefixosService();
			ReflectionTestUtils.setField(service, "caminhoArq", caminhoTeste);

			service.validarArquivo();

			assertTrue(Files.exists(Paths.get(caminhoTeste)));

			Files.deleteIfExists(Paths.get(caminhoTeste));
		} catch (Exception e) {
			System.out.println("Exceção capturada: " + e.getMessage());
			fail("Teste falhou");
		}
	}

	@Test
	@DisplayName("Deve criar o diretorio se ele nao existir.")
	public void validarDiretorio() {
		try {
			
			String diretorioAtual = System.getProperty("user.dir");
			File diretorioPrincipal = new File(diretorioAtual).getParentFile();
			String diretorio = diretorioPrincipal + "\\PrefixosTeste";

			PrefixosService service = new PrefixosService();
			ReflectionTestUtils.setField(service, "diretorio", diretorio);

			service.validarDiretorio();

			assertTrue(Files.exists(Paths.get(diretorio)));

			Files.deleteIfExists(Paths.get(diretorio));

		} catch (Exception e) {
			System.out.println("Exceção capturada: " + e.getMessage());
			fail("Exceção falhou");
		}
	}
}