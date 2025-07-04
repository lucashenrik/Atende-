package com.lucas.demo.infra.model;

import java.util.Objects;

public class Prefixo {
	private String prefixo;

	public Prefixo() {
	}

	public Prefixo(String prefixo) {
		this.prefixo = prefixo;
	}

	public String getPrefixo() {
		return prefixo;
	}

	public void setPrefixo(String prefixo) {
		this.prefixo = prefixo;
	}

	@Override
	public int hashCode() {
		return Objects.hash(prefixo);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Prefixo other = (Prefixo) obj;
		return Objects.equals(prefixo, other.prefixo);
	}
}