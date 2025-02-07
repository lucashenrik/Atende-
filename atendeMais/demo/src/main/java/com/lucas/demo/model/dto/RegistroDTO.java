package com.lucas.demo.model.dto;

import com.lucas.demo.model.EnumRoles;

public record RegistroDTO(String login, String senha, EnumRoles role) {
}