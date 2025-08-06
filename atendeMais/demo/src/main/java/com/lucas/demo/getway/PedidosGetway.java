package com.lucas.demo.getway;

import com.lucas.demo.infra.context.PedidosContext;
import com.lucas.demo.infra.context.ResultadoCarregamentoPedidosDTO;

import java.util.List;
import java.util.Map;

public interface PedidosGetway {
    boolean createNewOrder(String xml, String estabelecimentoId);
    boolean updateStatusOrder(int id, String newStatus, String estabelecimentoId);
    ResultadoCarregamentoPedidosDTO getOrders(String estabelecimentoId);
    List<Map<String, Object>> getOrdersNoDelivered(String estabelecimentoId);
    List<Map<String, Object>> getOrdersDelivered(String estabelecimentoId);
    List<Map<String, Object>> getOrdersForClients(String estabelecimentoId, List<String> idOrders);
    List<String> countOrders(String estabelecimentoId);
}