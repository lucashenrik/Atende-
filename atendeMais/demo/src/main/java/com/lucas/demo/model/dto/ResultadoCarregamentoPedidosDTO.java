package com.lucas.demo.model.dto;

import com.lucas.demo.model.PedidosContext;

public class ResultadoCarregamentoPedidosDTO {
	private boolean sucesso;
	private PedidosContext pedidosContext;
	
	public ResultadoCarregamentoPedidosDTO(boolean sucesso, PedidosContext pedidosContext) {
		this.sucesso = sucesso;
		this.pedidosContext = pedidosContext;
	}

	public boolean isSucesso() {
		return sucesso;
	}

	public PedidosContext getPedidosContext() {
		return pedidosContext;
	}
}