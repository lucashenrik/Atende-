package com.lucas.demo.infra.context;

import com.lucas.demo.domain.models.Estabelecimento;
import com.lucas.demo.infra.model.EstabelecimentoDB;

public class ConvertEstabelecimento {
    public static EstabelecimentoDB toInfraModel(Estabelecimento estabelecimento){
        return new EstabelecimentoDB(estabelecimento.getName(), estabelecimento.getTelefone(), estabelecimento.getEmail());
    }

    public static Estabelecimento toDomainModel(EstabelecimentoDB db){
        return new Estabelecimento(db.getNome(), db.getTelefone(), db.getEmail());
    }
}
