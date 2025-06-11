package com.lucas.demo.application;

import com.lucas.demo.getway.PedidosGetway;
import com.lucas.demo.infra.context.PedidosContext;
import com.lucas.demo.infra.context.ResultadoCarregamentoPedidosDTO;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PedidoUseCase {

    private PedidosGetway pedidosGetway;

    public PedidoUseCase(PedidosGetway pedidosGetway){
        this.pedidosGetway = pedidosGetway;
    }

    public boolean newOrder(String xml, String estabelecimentoId){
        return pedidosGetway.createNewOrder(xml, estabelecimentoId);
    }

    public boolean updateStatusOrder(String referenceId, String newStatus, String timestamp, String estabelecimentoId){
        return pedidosGetway.updateStatusOrder(referenceId, newStatus, timestamp, estabelecimentoId);
    }

    public List<String> countOrders(String estabelecimentoId){
       return pedidosGetway.countOrders(estabelecimentoId);
    }

    public List<Map<String, String>> getOrdersDelivered(String estabelecimentoId){
        return pedidosGetway.getOrdersDelivered(estabelecimentoId);
    }

    public List<Map<String, String>> getOrdersNoDelivered(String estabelecimentoId){
       return pedidosGetway.getOrdersNoDelivered(estabelecimentoId);
    }

    public List<Map<String, String>> getOrdersForClients(String estabelecimentoId, List<String> idOrders){
       return pedidosGetway.getOrdersForClients(estabelecimentoId, idOrders);
    }

    public ResultadoCarregamentoPedidosDTO getOrders(String estabelecimentoId){
        return pedidosGetway.getOrders(estabelecimentoId);
    }
}