package com.lucas.demo.service; 

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.fail;

/*
@ExtendWith(MockitoExtension.class)
public class ArquivoServiceTest {

    @InjectMocks
    private ArquivoService arquivoService;
    @Mock
    private PedidoServico pedidoServ;

    private LocalTime data;
    private String dataString;
    private ItemXml novoItem;

    // Cria um caminho de arquivo temporário para os testes.
    private final String baseDir = System.getProperty("java.io.tmpdir");
    // Usaremos um subdiretório "clientes/teste"
    private final String dirTeste = baseDir + File.separator + "clientes" + File.separator + "teste";
    // Caminho para o arquivo de teste; o ArquivoService adiciona a data, mas vamos forçar um nome fixo via stub.
    private final String caminhoTeste = dirTeste + File.separator + "teste.json";

    @BeforeEach
    public void setup() {
        // Define valores para os testes
        this.data = LocalTime.now();
        this.dataString = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH));
        // Cria um novo ItemXml para teste (referenceId=11, nome "Teste", status "cancelado", etc.)
        this.novoItem = new ItemXml(11, "Teste", 0, 5.0, "cancelado", data);

        // Cria o diretório de teste, se não existir
        File dir = new File(dirTeste);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        // Opcional: Limpa o arquivo de teste antes de cada execução (apagar se existir)
        File fileTeste = new File(caminhoTeste);
        if (fileTeste.exists()) {
            fileTeste.delete();
        }
    }

    @Test
    @DisplayName("Deve escrever os pedidos no arquivo")
    public void escreverPedidos() {
        try {
            // Cria um spy de ArquivoService para stub do método obterCaminhoArquivo
            ArquivoService spyArquivoService = Mockito.spy(arquivoService);
            when(spyArquivoService.obterCaminhoArquivo("teste")).thenReturn(caminhoTeste);

            // Chama o método e verifica que não lança exceção
            assertDoesNotThrow(() -> spyArquivoService.escreverPedido(novoItem, "teste"));
        } catch (Exception e) {
            e.printStackTrace();
            fail("Teste falhou: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Deve encontrar o arquivo de destino")
    public void verificarArquivo() {
        try {
            // Força o stub para obterCaminhoArquivo
            ArquivoService spyArquivoService = Mockito.spy(arquivoService);
            when(spyArquivoService.obterCaminhoArquivo("teste")).thenReturn(caminhoTeste);
            
            // Deve retornar uma lista (vazia se o arquivo não existir)
            assertDoesNotThrow(() -> spyArquivoService.carregarPedidosDoArquivo("teste"));
        } catch (Exception e) {
            e.printStackTrace();
            fail("Teste falhou: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Deve atualizar o status de um pedido")
    public void atualizarStatus() {
        try {
            // Para o novoItem, a reference_id é 11 e a hora é dataString
            String id = Integer.toString(novoItem.getReferenceId());

            // Simular que o pedido já existe no arquivo.
            // Cria um JSON com um pedido que possui reference_id "11" e hora igual a dataString.
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
            
            String pedidoJson = writer.writeValueAsString(
                java.util.List.of(
                    new java.util.HashMap<String, Object>() {{
                        put("reference_id", id);
                        put("hora", dataString);
                        put("status", "cancelado");
                        put("description", "Teste");
                        put("quantity", "0");
                    }}
                )
            );
            // Escreve o JSON no arquivo de teste
            Files.write(Paths.get(caminhoTeste), pedidoJson.getBytes());
            
            // Cria um mock de PedidoServico para ser chamado dentro de alterarStatus
            PedidoServico mockPedidoServ = Mockito.mock(PedidoServico.class);
            PedidosContext context = new PedidosContext(); // Pode configurar se necessário
            ResultadoCarregamentoPedidosDTO resultado = new ResultadoCarregamentoPedidosDTO(true, context);
            when(mockPedidoServ.carregarPedidos("teste")).thenReturn(resultado);

            ArquivoService spyArquivoServ = Mockito.spy(arquivoService);
            ReflectionTestUtils.setField(spyArquivoServ, "pedidoServ", mockPedidoServ);
            when(spyArquivoServ.obterCaminhoArquivo("teste")).thenReturn(caminhoTeste);

            // Chama o método alterarStatus
            boolean sucesso = spyArquivoServ.alterarStatus(id, "pronto", dataString, "teste");
            assertThat(sucesso).isTrue();
            Mockito.verify(mockPedidoServ).carregarPedidos("teste");
            
            // Limpa o arquivo de teste
            Files.deleteIfExists(Paths.get(caminhoTeste));
        } catch (Exception e) {
            e.printStackTrace();
            fail("Teste falhou: " + e.getMessage());
        }
    }
}
*/