package com.lucas.demo.model;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Item {

	@JsonProperty("reference_id")
	private int referenceId;
	@JsonProperty("name")
	private String name;
	@JsonProperty("quantity")
	private int quantity;
	@JsonProperty("status")
	private String status;
	@JsonProperty("hora")
	private LocalTime hora;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(int referenceId) {
		this.referenceId = referenceId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public LocalTime getHora() {
		return hora;
	}

	public void setHora(LocalTime hora) {
		this.hora = hora;
	}

	@Override
	public String toString() {
		return "Pedido{" + "referenceId='" + referenceId + '\'' + ", name='" + name + '\'' + ", quantity=" + quantity
				+ '/' + ", status=" + status + '/' + ", hora=" + hora + '\'' + '}';
	}
}