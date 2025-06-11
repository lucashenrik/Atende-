package com.lucas.demo.infra.controllers;

import com.lucas.demo.application.EstabelecimentoUseCase;
import com.lucas.demo.application.UserUserCase;
import com.lucas.demo.infra.context.ConvertEstabelecimento;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lucas.demo.infra.security.AuthorizationSecurity;
import com.lucas.demo.infra.security.TokenService;
import com.lucas.demo.infra.security.CustomUserDetails;
import com.lucas.demo.domain.models.EnumRoles;
import com.lucas.demo.infra.model.EstabelecimentoDB;
import com.lucas.demo.infra.model.UserDB;
import com.lucas.demo.infra.model.dto.AlterarRegistDTO;
import com.lucas.demo.infra.model.dto.LoginRequestDTO;
import com.lucas.demo.infra.model.dto.LoginResponseDTO;
import com.lucas.demo.infra.model.dto.NewRegisterDTO;
import com.lucas.demo.infra.model.dto.RegisterRequestDTO;
import com.lucas.demo.infra.repository.DepartRepository;
import com.lucas.demo.infra.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	private EstabelecimentoUseCase estabelecimentoUseCase;
	private UserUserCase userUserCase;
	private UserRepository userRepository;
	private DepartRepository estabelRepository;
	private PasswordEncoder passwordEncoder;
	private TokenService tokenService;
	private AuthenticationManager authenticationManager;
	private AuthorizationSecurity auth;

	public AuthController(EstabelecimentoUseCase estabelecimentoUseCase, UserUserCase userUserCase, UserRepository userRepository, DepartRepository estabelRepository,
						  PasswordEncoder passwordEncoder, TokenService tokenService, AuthenticationManager authenticationManager,
						  AuthorizationSecurity auth) {
		super();
		this.estabelecimentoUseCase = estabelecimentoUseCase;
		this.userUserCase = userUserCase;
		this.userRepository = userRepository;
		this.estabelRepository = estabelRepository;
		this.passwordEncoder = passwordEncoder;
		this.tokenService = tokenService;
		this.authenticationManager = authenticationManager;
		this.auth = auth;
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO body) {
		var authToken = new UsernamePasswordAuthenticationToken(body.email(), body.password());
		var authentication = authenticationManager.authenticate(authToken);

		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
		UserDB user = userDetails.getUserDB();
		String token = tokenService.generateToken(user);

		return ResponseEntity.ok(new LoginResponseDTO(token));
	}

	@PostMapping("/registrar-usuario")
	public ResponseEntity<Void> register(@RequestBody RegisterRequestDTO dto,
			@RequestHeader("Authorization") String authHeader) {
		String idEstabelecimento = auth.validarToken(authHeader);

		userUserCase.createNewUser(dto);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@PutMapping("/alterar")
	public ResponseEntity<?> alterar(@RequestBody AlterarRegistDTO dto,
			@RequestHeader("Authorization") String authHeader) {
		String idCliente = auth.validarToken(authHeader);
		if (idCliente == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		userUserCase.updateUser(dto);
		return ResponseEntity.status(HttpStatus.OK).build();
	}

	@PostMapping("/registrar-estabelecimento")
	public ResponseEntity<?> registro(@RequestBody NewRegisterDTO dto) {
		estabelecimentoUseCase.createNewEstabelecimento(dto);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}
}