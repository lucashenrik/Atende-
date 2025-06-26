package com.lucas.demo.application;

import com.lucas.demo.getway.PedidosGetway;
import com.lucas.demo.infra.context.PedidosContext;
import com.lucas.demo.infra.context.ResultadoCarregamentoPedidosDTO;
import com.lucas.demo.infra.model.NewNotificationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PedidoUseCase {

    private PedidosGetway pedidosGetway;
    private final ApplicationEventPublisher eventPublisher;

    public PedidoUseCase(PedidosGetway pedidosGetway, ApplicationEventPublisher eventPublisher){
        this.pedidosGetway = pedidosGetway;
        this.eventPublisher = eventPublisher;
    }

    public void newOrder(String xml, String estabelecimentoId){
        pedidosGetway.createNewOrder(xml, estabelecimentoId);
        eventPublisher.publishEvent(new NewNotificationEvent("Nova notificação recebida!"));
    }

    public boolean updateStatusOrder(String referenceId, String newStatus, String timestamp, String estabelecimentoId){
        boolean sucess = pedidosGetway.updateStatusOrder(referenceId, newStatus, timestamp, estabelecimentoId);
        eventPublisher.publishEvent(new NewNotificationEvent("Nova notificação recebida!"));
        return sucess;
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