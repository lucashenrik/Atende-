package com.lucas.demo.model;

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