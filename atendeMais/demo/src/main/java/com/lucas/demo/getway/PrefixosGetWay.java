package com.lucas.demo.getway;

import com.lucas.demo.infra.model.Prefixo;

import java.util.List;

public interface PrefixosGetWay {
    boolean createNewPrefixo(String estabelecimento, String prefixo);
    boolean deletePrefixo(String prefixo, String estabelecimentoId);
    List<Prefixo> getAllPrefixos();
    List<Prefixo> getAllPrefixos(String estabelecimentoId);
}