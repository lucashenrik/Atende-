package com.lucas.demo.model;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemXml {
	@JacksonXmlProperty(localName = "id")
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

	// Construtor vazio necessário para a desserialização
	public ItemXml() {
	}

	// Construtor que aceita strings para desserializar
	@JsonCreator
	public ItemXml(@JacksonXmlProperty(localName = "id") String referenceId,
			@JacksonXmlProperty(localName = "description") String name,
			@JacksonXmlProperty(localName = "quantity") String quantity,
			@JacksonXmlProperty(localName = "amount") String amount,
			@JacksonXmlProperty(localName = "hora") String hora) {
		this.referenceId = Integer.parseInt(referenceId);
		this.name = name;
		this.quantity = Integer.parseInt(quantity);
		this.amount = Double.parseDouble(amount);
		this.hora = LocalTime.parse(hora);
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
