package com.lucas.demo.infra.context;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.lucas.demo.infra.model.ItemXml;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PedidosXml {

	@JacksonXmlElementWrapper(useWrapping = true)
	@JacksonXmlProperty(localName = "item")
	private List<ItemXml> items;

	public PedidosXml() {
	}

	public List<ItemXml> getItems() {
		return items;
	}

	public void setItems(List<ItemXml> items) {
		this.items = items;
	}

	@Override
	public String toString() {
		return "Pedidos{" + "items=" + items + '}';
	}
}