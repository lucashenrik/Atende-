package com.lucas.demo.domain.models;

public enum EnumRoles {
	ADMIN("admin"), USER("user");

	private String role;

	private EnumRoles(String role) {
		this.role = role;
	}

	public String getRole() {
		return role;
	}
}