package com.lucas.demo.getway;

import com.lucas.demo.infra.context.PedidosContext;
import com.lucas.demo.infra.context.ResultadoCarregamentoPedidosDTO;

import java.util.List;

public interface PedidosGetway {
    boolean createNewOrder(String xml, String estabelecimentoId);
    boolean updateStatusOrder(String referenceId, String newStatus, String timestamp, String estabelecimentoId);
    ResultadoCarregamentoPedidosDTO getOrders(String estabelecimentoId);
    List<String> count(PedidosContext  pedidosContext);
}
