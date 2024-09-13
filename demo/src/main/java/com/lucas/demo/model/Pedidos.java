package com.lucas.demo.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Pedidos {

	@JsonProperty("items")
	private List<Item> items = new ArrayList<>();

	public Pedidos() {
	}

	public Pedidos(List<Item> items) {
		this.items = items;
	}

	public List<Item> getItems() {
		return items;
	}

	@Override
	public String toString() {
		return "Pedidos{" + "items=" + items + '}';
	}
}