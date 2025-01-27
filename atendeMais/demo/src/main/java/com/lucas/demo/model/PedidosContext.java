package com.lucas.demo.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PedidosContext {
	protected List<Map<String, String>> pedidosAll = new ArrayList<>();
	protected List<Map<String, String>> pedidosVerificados = new ArrayList<>();
	private List<Map<String, String>> pedidosEntregues = new ArrayList<>();
	private List<Map<String, String>> pedidosCancelados = new ArrayList<>();

	public List<Map<String, String>> getPedidosAll() {
		return pedidosAll;
	}

	public List<Map<String, String>> getPedidosVerificados() {
		return pedidosVerificados;
	}

	public List<Map<String, String>> getPedidosEntregues() {
		return pedidosEntregues;
	}

	public List<Map<String, String>> getPedidosCancelados() {
		return pedidosCancelados;
	}

	public boolean isEmpty() {
		return (pedidosEntregues == null || pedidosEntregues.isEmpty())
				&& (pedidosCancelados == null || pedidosCancelados.isEmpty())
				&& (pedidosAll == null || pedidosAll.isEmpty());
	}

}