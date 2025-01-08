package com.lucas.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.system.SystemProperties;
import org.springframework.test.util.ReflectionTestUtils;

import com.lucas.demo.model.ItemXml;

@ExtendWith(MockitoExtension.class)
public class ArquivoServiceTest {

	@Autowired
	@InjectMocks
	private ArquivoService arquivoService;

	@Mock
	private PedidoServico pedidoServ;

	private LocalTime data;
	private String dataString;
	private ItemXml novoItem;

	private final String caminhoAtual = SystemProperties.get("user.dir");
	private final String caminhoSup = new File(caminhoAtual).getParentFile().toString();
	private final String caminhoTeste = caminhoSup + "\\Teste\\teste.json";

	@BeforeEach
	public void setup() {
		this.data = LocalTime.now();
		this.dataString = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
		this.novoItem = new ItemXml(11, "Teste", 0, 5.0, "cancelado", data);
	}

	@Test
	@DisplayName("Deve escrever os pedidos no arquivo")
	public void escreverPedidos() {
		try {
			ArquivoService spyArquivoService = Mockito.spy(ArquivoService.class);

			when(spyArquivoService.verificarHora()).thenReturn(caminhoTeste);

			assertDoesNotThrow(() -> spyArquivoService.escreverPedido(novoItem));
		} catch (Exception e) {
			System.out.println("Excecao capturada: " + e.getMessage());
			e.printStackTrace();
			fail("Teste falhou");
		}
	}

	@Test
	@DisplayName("Deve encontrar o arquivo de destino")
	public void verificarArquivo() {
		arquivoService.verificarArquivo();
		assertDoesNotThrow(() -> arquivoService.verificarArquivo());
	}

	@Test
	@DisplayName("Deve atualizar o status de um pedido")
	public void atualizarStatus() {
		try {
			String id = Integer.toString(novoItem.getReferenceId());

			PedidoServico mockPedidoServ = Mockito.mock(PedidoServico.class);
			when(mockPedidoServ.carregarPedidos()).thenReturn(true);

			ArquivoService spyArquivoServ = Mockito.spy(ArquivoService.class);
			ReflectionTestUtils.setField(spyArquivoServ, "pedidoServ", mockPedidoServ);
			when(spyArquivoServ.verificarHora()).thenReturn(caminhoTeste);

			boolean sucesso = spyArquivoServ.alterarStatus(id, "pronto", dataString);

			assertThat(sucesso).isTrue();
			Mockito.verify(mockPedidoServ).carregarPedidos();
			
			Files.deleteIfExists(Paths.get(caminhoTeste));
		} catch (Exception e) {
			System.out.println("Excecao capturada: " + e.getMessage());
			e.printStackTrace();
			fail("Teste falhou");
		}
	}
}