package com.lucas.demo.application;

import com.lucas.demo.domain.exceptions.ErroDepartmentException;
import com.lucas.demo.domain.exceptions.ErroUserException;
import com.lucas.demo.domain.models.Estabelecimento;
import com.lucas.demo.domain.models.User;
import com.lucas.demo.getway.DepartmentGetWay;
import com.lucas.demo.infra.model.dto.AlterarRegistDTO;
import com.lucas.demo.infra.model.dto.NewRegisterDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EstabelecimentoUseCase {

    private DepartmentGetWay departmentGetWay;

    public EstabelecimentoUseCase(DepartmentGetWay departmentGetWay){
        this.departmentGetWay = departmentGetWay;
    }

    public void createNewEstabelecimento(NewRegisterDTO estabelecimento){
        departmentGetWay.createNewDepartment(estabelecimento);
    }

    public Estabelecimento getEstabByEmail(String email){
        return departmentGetWay.findByEmail(email)
                .orElseThrow(() -> new ErroDepartmentException("Nenhum estabelecimento encontrado com esse email."));
    }

    public void deleteEstabByEmail(String email){
        Estabelecimento estabelecimento = departmentGetWay.findByEmail(email)
                .orElseThrow(() -> new ErroDepartmentException("Nenhum apartamento encontrado com o email: " + email));
        departmentGetWay.deleteByEmail(estabelecimento.getEmail());
    }
}