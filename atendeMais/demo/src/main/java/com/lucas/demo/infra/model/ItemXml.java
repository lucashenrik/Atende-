package com.lucas.demo.infra.model;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemXml {

	@JacksonXmlProperty(localName = "reference_id")
	private int referenceId;

	@JacksonXmlProperty(localName = "description")
	private String name;

	@JacksonXmlProperty(localName = "quantity")
	private int quantity;

	@JacksonXmlProperty(localName = "amount")
	private double amount;

	@JacksonXmlProperty(localName = "status")
	private String status;

	@JacksonXmlProperty(localName = "hora")
	private LocalTime hora;

	public ItemXml() {
	}

	public ItemXml(int referenceId, String name, int quantity, double amount, String status, LocalTime hora) {
		this.referenceId = referenceId;
		this.name = name;
		this.quantity = quantity;
		this.amount = amount;
		this.status = status;
		this.hora = hora;
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

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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