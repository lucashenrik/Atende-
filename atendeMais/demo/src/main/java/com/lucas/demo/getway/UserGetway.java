package com.lucas.demo.getway;

import com.lucas.demo.domain.models.User;
import com.lucas.demo.infra.model.dto.AlterarRegistDTO;
import com.lucas.demo.infra.model.dto.RegisterRequestDTO;

import java.util.Optional;

public interface UserGetway {
    void save(User user);
    void deleteByEmail(String email);
    void updateUser(AlterarRegistDTO dto);
    Optional<User> findByEmail(String email);
}