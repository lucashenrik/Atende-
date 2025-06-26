package com.lucas.demo.application;

import com.lucas.demo.domain.exceptions.ErroUserException;
import com.lucas.demo.domain.models.EnumRoles;
import com.lucas.demo.domain.models.User;
import com.lucas.demo.getway.UserGetway;
import com.lucas.demo.infra.model.dto.AlterarRegistDTO;
import com.lucas.demo.infra.model.dto.RegisterRequestDTO;
import org.springframework.stereotype.Component;

@Component
public class UserUserCase {

    private UserGetway userGetway;

    public UserUserCase(UserGetway userGetway){
        this.userGetway = userGetway;
    }

    public void createNewUser(RegisterRequestDTO dto){
        if (userGetway.findByEmail(dto.email()).isEmpty()){
            User user = new User(dto.name(), dto.email(), dto.password(), EnumRoles.ADMIN);
            userGetway.save(user);
        } else {
            throw new ErroUserException("Este email já esta em uso.");
        }
    }

    public void deleteUser(String email){
        User user = userGetway.findByEmail(email)
                .orElseThrow(() -> new ErroUserException("Nenhum usuario encontrado para deletar."));
        userGetway.deleteByEmail(user.getEmail());
    }

    public User getUserByEmail(String email){
        return userGetway.findByEmail(email)
                .orElseThrow(() -> new ErroUserException("Nenhum usuario encontrado com esse email."));
    }

    public void updateUser(AlterarRegistDTO dto){
        userGetway.findByEmail(dto.email())
                .orElseThrow(() -> new ErroUserException("Nenhum usuario encontrado com esse email."));
        userGetway.updateUser(dto);
    }
}