package com.lucas.demo.infra.context;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PedidosContext {
	protected List<Map<String, Object>> pedidosAll = new ArrayList<>();
	protected List<Map<String, Object>> pedidosVerificados = new ArrayList<>();
	private List<Map<String, Object>> pedidosEntregues = new ArrayList<>();
	private List<Map<String, Object>> pedidosCancelados = new ArrayList<>();

	public List<Map<String, Object>> getPedidosAll() {
		return pedidosAll;
	}

	public List<Map<String, Object>> getPedidosVerificados() {
		return pedidosVerificados;
	}

	public List<Map<String, Object>> getPedidosEntregues() {
		return pedidosEntregues;
	}

	public List<Map<String, Object>> getPedidosCancelados() {
		return pedidosCancelados;
	}

	public boolean isEmpty() {
		return (pedidosEntregues == null || pedidosEntregues.isEmpty())
				&& (pedidosCancelados == null || pedidosCancelados.isEmpty())
				&& (pedidosAll == null || pedidosAll.isEmpty());
	}

}