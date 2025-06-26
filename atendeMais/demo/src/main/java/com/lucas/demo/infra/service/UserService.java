package com.lucas.demo.infra.service;

import com.lucas.demo.domain.models.EnumRoles;
import com.lucas.demo.domain.models.User;
import com.lucas.demo.getway.UserGetway;
import com.lucas.demo.infra.context.ConvertUser;
import com.lucas.demo.infra.model.UserDB;
import com.lucas.demo.infra.model.dto.AlterarRegistDTO;
import com.lucas.demo.infra.repository.DepartRepository;
import com.lucas.demo.infra.repository.UserRepository;
import com.lucas.demo.infra.security.CustomUserDetails;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService implements UserGetway {

    private UserRepository userRepository;
    private DepartRepository estabelRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;

    public UserService(UserRepository userRepository, DepartRepository estabelRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.estabelRepository = estabelRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public void save(User user) {
        String senhaCriptografada = passwordEncoder.encode(user.getPassword());
        UserDB novoUser = new UserDB(user.getName(), user.getEmail(), senhaCriptografada, EnumRoles.USER);
        userRepository.save(novoUser);
    }

    @Override
    public void deleteByEmail(String email) {
        userRepository.deleteByEmail(email);
    }

    @Override
    public void updateUser(AlterarRegistDTO dto) {

        // Autentica o usuário com as credenciais fornecidas
        var authToken = new UsernamePasswordAuthenticationToken(dto.email(), dto.senhaAntiga());
        var authentication = authenticationManager.authenticate(authToken);
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UserDB user = userDetails.getUserDB();

        // Atualiza a senha
        String novaSenhaCriptografada = passwordEncoder.encode(dto.novaSenha());
        user.setName(dto.nome());
        user.setPassword(novaSenhaCriptografada);
        userRepository.save(user);

    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(ConvertUser::toDomainModel);
    }
}