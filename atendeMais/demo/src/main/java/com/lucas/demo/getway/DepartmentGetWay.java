package com.lucas.demo.getway;

import com.lucas.demo.domain.models.Estabelecimento;
import com.lucas.demo.infra.model.dto.NewRegisterDTO;

import java.util.Optional;

public interface DepartmentGetWay {
    void createNewDepartment(NewRegisterDTO estabelecimento);
    void save(Estabelecimento estabelecimento);
    void deleteByEmail(String email);
    Optional<Estabelecimento> findByEmail(String email);
}