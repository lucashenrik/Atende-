package com.lucas.demo.infra.service;

import com.lucas.demo.domain.exceptions.ErroDepartmentException;
import com.lucas.demo.domain.models.EnumRoles;
import com.lucas.demo.domain.models.Estabelecimento;
import com.lucas.demo.getway.DepartmentGetWay;
import com.lucas.demo.infra.context.ConvertEstabelecimento;
import com.lucas.demo.infra.model.EstabelecimentoDB;
import com.lucas.demo.infra.model.UserDB;
import com.lucas.demo.infra.model.dto.AlterarRegistDTO;
import com.lucas.demo.infra.model.dto.NewRegisterDTO;
import com.lucas.demo.infra.repository.DepartRepository;
import com.lucas.demo.infra.repository.UserRepository;
import com.lucas.demo.infra.security.CustomUserDetails;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DepartmentService implements DepartmentGetWay {

    private PasswordEncoder passwordEncoder;
    private UserRepository userRepository;
    private DepartRepository estabelRepository;
    private AuthenticationManager authenticationManager;

    public DepartmentService(PasswordEncoder passwordEncoder, UserRepository userRepository, DepartRepository estabelRepository, AuthenticationManager authenticationManager) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.estabelRepository = estabelRepository;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public void createNewDepartment(NewRegisterDTO dto) {
        if (userRepository.findByEmail(dto.email()).isEmpty()) {
            String senhaCriptografada = passwordEncoder.encode(dto.password());
            EstabelecimentoDB novoEstabelecimento = new EstabelecimentoDB(dto.name(), dto.telefone(), dto.email());
            estabelRepository.save(novoEstabelecimento);

            EstabelecimentoDB estabelecimento = this.findByEmail(dto.email())
                    .map(ConvertEstabelecimento::toInfraModel)
                    .orElseThrow(() -> new ErroDepartmentException("Nenhum departamento encontrado com o email: " + dto.email()));

            UserDB novoUsuario = new UserDB(dto.name(), dto.email(), senhaCriptografada, EnumRoles.ADMIN);
            userRepository.save(novoUsuario);
        }
    }

    @Override
    public Optional<Estabelecimento> findByEmail(String email) {
        return estabelRepository.findByEmail(email)
                .map(ConvertEstabelecimento::toDomainModel);
    }

    @Override
    public void save(Estabelecimento estabelecimento) {
        estabelRepository.save(ConvertEstabelecimento.toInfraModel(estabelecimento));
    }

    @Override
    public void deleteByEmail(String email) {
        estabelRepository.deleteByEmail(email);
    }
}