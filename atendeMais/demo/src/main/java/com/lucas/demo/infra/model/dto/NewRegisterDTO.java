package com.lucas.demo.infra.model.dto;

import com.lucas.demo.domain.models.Estabelecimento;

public record NewRegisterDTO(String name, String telefone, String email, String password) {

    public Estabelecimento toDomainModel(){
        return new Estabelecimento(name, telefone, email);
    }
}