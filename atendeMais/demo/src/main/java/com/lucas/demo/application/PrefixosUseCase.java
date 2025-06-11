package com.lucas.demo.application;

import com.lucas.demo.getway.PrefixosGetWay;
import com.lucas.demo.infra.model.Prefixo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PrefixosUseCase {

    private PrefixosGetWay prefixosGetWay;

    public PrefixosUseCase(PrefixosGetWay prefixosGetWay){
        this.prefixosGetWay = prefixosGetWay;
    }

    public boolean createNewPrefixo(String estabelecimentoId, String prefixo) {
        return prefixosGetWay.createNewPrefixo(estabelecimentoId, prefixo);
    }

    public List<Prefixo> getAllPrefixos(String estabelecimentoId) {
        return prefixosGetWay.getAllPrefixos(estabelecimentoId);
    }

    public List<Prefixo> carregarPrefixos() {
        return prefixosGetWay.getAllPrefixos();
    }

    public boolean deletePrefixo(String prefixo, String estabelecimentoId) {
        return prefixosGetWay.deletePrefixo(prefixo, estabelecimentoId);
    }
}