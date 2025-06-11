package com.lucas.demo.infra.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.lucas.demo.domain.exceptions.ErroArquivoException;
import com.lucas.demo.domain.exceptions.ErroProcessamentoException;
import com.lucas.demo.getway.PedidosGetway;
import com.lucas.demo.infra.context.CaminhoInfo;
import com.lucas.demo.infra.context.PedidosContext;
import com.lucas.demo.infra.context.PedidosEFile;
import com.lucas.demo.infra.context.ResultadoCarregamentoPedidosDTO;
import com.lucas.demo.infra.model.ItemXml;
import com.lucas.demo.infra.model.Prefixo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PedidoService implements PedidosGetway {

    private ArquivoService arquivoService;
    @Lazy
    private PrefixosService prefixosService;

    private ObjectMapper mapper = new ObjectMapper();
    private final XmlMapper xmlMapper = new XmlMapper();
    private final com.fasterxml.jackson.databind.ObjectMapper jsonMapper = new com.fasterxml.jackson.databind.ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(PedidoService.class);

    public PedidoService(ArquivoService arquivoService, PrefixosService prefixosService){
        this.arquivoService = arquivoService;
        this.prefixosService = prefixosService;
    }

    @Override
    public boolean createNewOrder(String xml, String estabelecimentoId) {
        boolean sucesso = false;
        try {
            JsonNode rootNode = xmlMapper.readTree(xml); // Converte o XML para uma árvore JSON para navegar nos nós
            Iterable<JsonNode> itemsNodes = rootNode.findValues("items"); // Acessa todos os nós "items" dentro do nó
            // raiz

            if (!itemsNodes.iterator().hasNext()) { // Verifica se encontrou nós "items"
                throw new ErroProcessamentoException("O XML enviado não contém o campo 'items' ou está malformado.");
            }

            List<ItemXml> itensProcessados = new ArrayList<>();
            for (JsonNode itemsNode : itemsNodes) { // Processa cada nó "items" encontrado
                JsonNode itemNode = itemsNode.path("item");
                if (itemNode.isArray()) { // Verifica se 'item' é um array ou objeto único
                    for (JsonNode unicoItemNode : itemNode) {
                        this.processarItemNode(unicoItemNode, itensProcessados, estabelecimentoId);
                    }
                } else {
                    this.processarItemNode(itemNode, itensProcessados, estabelecimentoId);
                }
            }

            for (ItemXml item : itensProcessados) { // Escreve os itens processados
                try {
                    arquivoService.newPedido(item, estabelecimentoId);
                    sucesso = true;
                } catch (ErroArquivoException e) {
                    logger.error("Erro ao abrir ou escrever no arquivo: {}", e.getMessage(), e);
                } catch (Exception e) {
                    logger.error("Erro inesperado ao salvar o item: {}", e.getMessage(), e);
                }
            }
            return sucesso;
        } catch (IOException e) {
            throw new ErroProcessamentoException("Erro ao ler o XML", e);
        } catch (ErroProcessamentoException e) {
            throw new ErroProcessamentoException("Erro: {}", e);
        } catch (Exception e) {
            throw new ErroProcessamentoException("Erro inesperado ao processar o XML", e);
        }
    }

    // Método auxiliar para processar um único itemNode
    private void processarItemNode(JsonNode itemNode, List<ItemXml> itensProcessados, String estabelecimentoId) {
        try {
            // Verifica se os campos obrigatórios existem e são válidos
            if (itemNode.hasNonNull("id") && itemNode.hasNonNull("description") && itemNode.hasNonNull("quantity")) {
                // Converte o nó JSON em objeto ItemXml
                ItemXml novoItem = xmlMapper.treeToValue(itemNode, ItemXml.class);
                // Verifica se o novoItem não é nulo e se o nome segue um prefixo esperado
                if (novoItem != null && this.comecaComPrefixo(novoItem.getName(), estabelecimentoId)) {
                    this.adicionarOuAtualizarItem(novoItem, itensProcessados);
                } else {
                    logger.info("Item ignorado (nulo ou sem nenhum dos prefixos): {}", itemNode.toString());
                }
            } else {
                logger.warn("Item malformado: {}", itemNode.toString());
            }
        } catch (Exception e) {
            logger.error("Erro ao processar item: {}", e.getMessage(), e);
        }
    }

    // Método auxiliar para adicionar um novo item ou atualizar a quantidade na
    // lista
    private void adicionarOuAtualizarItem(ItemXml novoItem, List<ItemXml> itensProcessados) {
        ItemXml itemExistente = this.encontrarItemNaLista(itensProcessados, novoItem);
        if (itemExistente != null) {
            // Atualiza a quantidade se o item já existir no nó
            itemExistente.setQuantity(itemExistente.getQuantity() + novoItem.getQuantity());
        } else {
            itensProcessados.add(novoItem);
        }
    }

    // Método para encontrar um item com o mesmo nome e ID de referência na lista
    private ItemXml encontrarItemNaLista(List<ItemXml> items, ItemXml novoItem) {
        return items.stream().filter(p -> p.getName().equals(novoItem.getName())).findFirst().orElse(null);
    }

    // Método para retornar os pedidos DTO
    @Override
    public synchronized ResultadoCarregamentoPedidosDTO getOrders(String estabelecimentoId) {
        LocalDate hoje  = LocalDate.now();
        LocalTime agora = LocalTime.now();
        LocalDate dataAnterior = hoje.minusDays(1);
        String caminhoAtual = this.verificarHora(estabelecimentoId); // Pega o caminho do arquivo baseado na hora atual
        String caminhoAnterior = this.caminhoArquivo(dataAnterior, estabelecimentoId); // Caminho do dia anterior
        PedidosContext pedidosContext = new PedidosContext();
        boolean sucesso = true;

        try {
            // Carregar pedidos do dia anterior somente se a hora for antes das 7h
            if (agora.isBefore(LocalTime.of(7, 0))) {
                pedidosContext = this.carregarPedidosDeArquivo(caminhoAnterior);
            }
            // Carregar pedidos do dia atual
            pedidosContext = this.carregarPedidosDeArquivo(caminhoAtual);
        } catch (ErroArquivoException e) {
            sucesso = false;
            logger.error("Erro ao carregar pedidos: {}", e.getMessage());
        }
        return new ResultadoCarregamentoPedidosDTO(sucesso, pedidosContext);
    }

    @Override
    public synchronized boolean updateStatusOrder(String referenceId, String novoStatus, String hora, String estabelecimentoId) {
        boolean sucesso = false;
        try {
            PedidosEFile payload = arquivoService.getPedidos(estabelecimentoId);
            boolean pedidoEncontrado = this.atualizarStatusPedido(referenceId, novoStatus, hora, payload);
            if (pedidoEncontrado) {
                mapper.writeValue(payload.file(), payload.pedidos());
                sucesso = true;
                this.getOrders(estabelecimentoId); // Recarregar pedidos para refletir as alterações
            }
        } catch (IOException e) {
            logger.error("Erro ao alterar status: {}", e.getMessage());
        }
        return sucesso;
    }

    protected boolean atualizarStatusPedido(String referenceId, String novoStatus, String hora, PedidosEFile payload) {
        // Percorre os pedidos e altera o status do pedido com a senha e hora
        // especificadas
        for (Map<String, Object> pedido : payload.pedidos()) {
            String ref = String.valueOf(pedido.get("reference_id"));
            String pedidoHora = String.valueOf(pedido.get("hora"));
            if (ref.equals(referenceId) && pedidoHora.equals(hora)) {
                pedido.put("status", novoStatus);
                if ("cancelar".equalsIgnoreCase(novoStatus) || "entregue".equalsIgnoreCase(novoStatus)) {
                    String horaAtual = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                    pedido.put("hora", horaAtual);
                }
                return true;
            }
        }
        return false;
    }

    // Método para lêr o arquivo JSON e distribui os pedidos conforme o status
    public PedidosContext carregarPedidosDeArquivo(String caminhoArquivo) throws ErroArquivoException {
        PedidosContext pedidoContext = new PedidosContext();
        File arquivo = new File(caminhoArquivo);

        if (!arquivo.exists()) {
            throw new ErroArquivoException("Arquivo " + caminhoArquivo + " não encontrado.");
        }

        try {
            List<Map<String, String>> pedidosArquivo = jsonMapper.readValue(arquivo,
                    new TypeReference<List<Map<String, String>>>() {
                    });
            for (Map<String, String> pedido : pedidosArquivo) {
                pedidoContext.getPedidosAll().add(pedido);
                String statusItem = pedido.get("status");
                if ("entregue".equals(statusItem)) {
                    if (!pedidoContext.getPedidosEntregues().contains(pedido)) {
                        pedidoContext.getPedidosEntregues().add(pedido);
                    }
                } else if ("cancelar".equals(statusItem)) {
                    if (!pedidoContext.getPedidosCancelados().contains(pedido)) {
                        pedidoContext.getPedidosCancelados().add(pedido);
                    }
                } else {
                    if (!pedidoContext.getPedidosVerificados().contains(pedido)) {
                        pedidoContext.getPedidosVerificados().add(pedido);
                    }
                }
            }
        } catch (IOException e) {
            throw new ErroArquivoException("Falha ao carregar pedidos do arquivo " + caminhoArquivo, e);
        }
        return pedidoContext;
    }

    @Override
    public List<String> count(PedidosContext pedidosContext) {
        return this.contar(pedidosContext.getPedidosVerificados(), "andamento");
    }

    public List<String> contar(List<Map<String, String>> pedidos, String statusDesejado) {
        return this.contar(pedidos, statusDesejado);
    }

    public List<String> contarPorStatus(List<Map<String, String>> pedidos, String statusDesejado) {
        Map<String, Integer> contagemItems = new HashMap<>();
        for (Map<String, String> pedido : pedidos) {
            if (pedido == null || pedido.isEmpty())
                continue;
            String descricao = pedido.get("description");
            String primeiroNome = descricao.split(" ")[0];
            String status = pedido.get("status");
            if ((statusDesejado.equals(status))) {
                int quantidade = Integer.parseInt(pedido.get("quantity"));
                // Verifica se o item já foi contado, se sim, incrementa, senão adiciona
                contagemItems.put(primeiroNome, contagemItems.getOrDefault(primeiroNome, 0) + quantidade);
            }
        }
        return contagemItems.entrySet().stream().map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.toList());
    }

    protected String caminhoArquivo(LocalDate data, String estabelecimentoId) {
        CaminhoInfo caminhoInfo = MudancaSO.obterCaminhoPedidos(estabelecimentoId);
        return caminhoInfo.getCaminhoArquivo() + data + ".json";
    }

    private String verificarHora(String estabelecimentoId) {
        CaminhoInfo caminhoInfo = MudancaSO.obterCaminhoPedidos(estabelecimentoId);
        String caminhoBase = caminhoInfo.getCaminhoArquivo();
        LocalDate data = LocalTime.now().isBefore(LocalTime.of(7, 0)) ? LocalDate.now().minusDays(1) : LocalDate.now();
        return caminhoBase + data + ".json";
    }

    public boolean comecaComPrefixo(String descricao, String estabelecimentoId) {
        List<String> prefixos = this.carregarPrefixosString(estabelecimentoId);
        return descricao != null && prefixos.stream().anyMatch(descricao::startsWith);
    }

    private List<String> carregarPrefixosString(String estabelecimentoId) {
        List<Prefixo> listaPrefixos = prefixosService.getAllPrefixos(estabelecimentoId);
        return listaPrefixos.stream().map(Prefixo::getPrefixo).collect(Collectors.toList());
    }
}