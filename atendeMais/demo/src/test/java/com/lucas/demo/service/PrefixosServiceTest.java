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

import com.lucas.demo.infra.service.PrefixosService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.lucas.demo.infra.model.Prefixo;

@ExtendWith(MockitoExtension.class)
public class PrefixosServiceTest {

	@Mock
	private ObjectMapper objectMapper;

	@InjectMocks
	private PrefixosService prefixosServ;
	
	public PrefixosServiceTest(PrefixosService prefixosServ) {
		super();
		this.prefixosServ = prefixosServ;
	}

	List<Prefixo> listaPrefixos = new ArrayList<>(Arrays.asList(new Prefixo("123"), new Prefixo("234")));
	List<Prefixo> listaPrefixosMock = new ArrayList<>(Arrays.asList(new Prefixo("Batata"), new Prefixo("Jantinha")));

	String caminhoAtual = System.getProperty("user.dir");
	String caminhoSup = new File(caminhoAtual).getParentFile().toString();

	String caminhoTeste = caminhoSup + "\\clientes\\teste\\prefixos\\teste.json";
	
	@SuppressWarnings("unchecked")
	@Test
	@DisplayName("Deve adicionar um novo prefixo")
	public void adicionarPrefixo() {

		try {
			when(objectMapper.readValue(any(File.class), any(TypeReference.class))).thenReturn(listaPrefixos);

			ObjectWriter writerMock = mock(ObjectWriter.class);

			when(objectMapper.writerWithDefaultPrettyPrinter()).thenReturn(writerMock);

			doNothing().when(writerMock).writeValue(any(File.class), any());

			boolean sucesso = prefixosServ.createNewPrefixo("434", "123");

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

			List<Prefixo> prefixosCarrregados = prefixosServ.getAllPrefixos("teste@gmail.com");

			assertNotNull(prefixosCarrregados);
			assertEquals(2, prefixosCarrregados.size());
			assertThat(prefixosCarrregados).extracting(Prefixo::getPrefixo).contains("Batata", "Jantinha");

			verify(objectMapper).readValue(any(File.class), any(TypeReference.class));
		} catch (Exception e) {
			System.out.println("Exceção capturada: " + e.getMessage());
		}
	}

	@Test
	@DisplayName("Deve excluir um prefixo")
	public void excluirPrefixo() {
		PrefixosService spyPrefixosServ = Mockito.spy(prefixosServ);

		doReturn(listaPrefixosMock).when(spyPrefixosServ).getAllPrefixos("teste@gmail.com");
		
		doNothing().when(spyPrefixosServ).salvarPrefixosNoArquivo(anyList(), caminhoTeste);

		boolean sucesso = spyPrefixosServ.deletePrefixo("Batata", "teste@gmail.com");

		assertTrue(sucesso);
		assertThat(listaPrefixosMock).extracting(Prefixo::getPrefixo).doesNotContain("Batata");

		verify(spyPrefixosServ).getAllPrefixos("teste@gmail.com");
		verify(spyPrefixosServ).salvarPrefixosNoArquivo(listaPrefixosMock, caminhoTeste);
	}

	@Test
	@DisplayName("Deve salvar prefixos no arquivo.")
	public void salvarPrefixosNoArquivo() {
		try {
			ObjectMapper mockObjectMapper = mock(ObjectMapper.class);

			PrefixosService spyPrefixosService = Mockito.spy(PrefixosService.class);
			ReflectionTestUtils.setField(spyPrefixosService, "objectMapper", mockObjectMapper);

			spyPrefixosService.salvarPrefixosNoArquivo(listaPrefixosMock, caminhoTeste);

			verify(mockObjectMapper).writeValue(any(File.class), eq(listaPrefixosMock));
		} catch (Exception e) {
			System.out.println("Exceção capturada: " + e.getMessage());
		}
	}

	@Test
	@DisplayName("Deve criar o arquivo se ele não existir.")
	public void validarCriarArquivo() throws IOException {
		String caminhoAtual = System.getProperty("user.dir");
		String caminhoSup = new File(caminhoAtual).getParentFile().toString();

		String caminhoTeste = caminhoSup + "\\clientes\\teste\\prefixos\\teste.json";

		PrefixosService service = new PrefixosService();
		ReflectionTestUtils.setField(service, "caminhoArq", caminhoTeste);

		service.validarArquivo(caminhoTeste);

		assertTrue(Files.exists(Paths.get(caminhoTeste)));

		Files.deleteIfExists(Paths.get(caminhoTeste));
	}

	@Test
	@DisplayName("Deve criar o diretorio se ele nao existir.")
	public void validarDiretorio() {
		String diretorioAtual = System.getProperty("user.dir");
		File diretorioPrincipal = new File(diretorioAtual).getParentFile();
		String diretorio = diretorioPrincipal + "\\clientes\\teste\\teste_prefixos";

		PrefixosService service = new PrefixosService();
		ReflectionTestUtils.setField(service, "diretorio", diretorio);

		service.validarDiretorio(diretorio);

		assertTrue(Files.exists(Paths.get(diretorio)));

		try {
			Files.deleteIfExists(Paths.get(diretorio));
		} catch (IOException e) {
			e.printStackTrace();

		}
	}
}