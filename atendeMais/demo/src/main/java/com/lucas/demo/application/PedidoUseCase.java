package com.lucas.demo.application;

import com.lucas.demo.getway.PedidosGetway;
import com.lucas.demo.infra.context.PedidosContext;
import com.lucas.demo.infra.context.ResultadoCarregamentoPedidosDTO;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import java.util.List;

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

    public ResultadoCarregamentoPedidosDTO getOrders(String estabelecimentoId){
        return pedidosGetway.getOrders(estabelecimentoId);
    }

    public List<String> count(PedidosContext context){
        return pedidosGetway.count(context);
    }
}