package com.lucas.demo.getway;

import com.lucas.demo.infra.model.ItemXml;
import com.lucas.demo.infra.context.PedidosEFile;

public interface ArquivoGetway {
    void newPedido(ItemXml item, String estabelecimentoId);
    PedidosEFile getPedidos(String estabelecimento);
}