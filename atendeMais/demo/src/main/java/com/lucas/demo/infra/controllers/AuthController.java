package com.lucas.demo.infra.controllers;

import com.lucas.demo.application.EstabelecimentoUseCase;
import com.lucas.demo.application.UserUserCase;
import com.lucas.demo.domain.exceptions.LoginInvalidoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lucas.demo.infra.security.TokenService;
import com.lucas.demo.infra.security.CustomUserDetails;
import com.lucas.demo.infra.model.UserDB;
import com.lucas.demo.infra.model.dto.AlterarRegistDTO;
import com.lucas.demo.infra.model.dto.LoginRequestDTO;
import com.lucas.demo.infra.model.dto.LoginResponseDTO;
import com.lucas.demo.infra.model.dto.NewRegisterDTO;
import com.lucas.demo.infra.model.dto.RegisterRequestDTO;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	private EstabelecimentoUseCase estabelecimentoUseCase;
	private UserUserCase userUserCase;
	private PasswordEncoder passwordEncoder;
	private TokenService tokenService;
	private AuthenticationManager authenticationManager;

	@Autowired
	public AuthController(EstabelecimentoUseCase estabelecimentoUseCase, UserUserCase userUserCase, TokenService tokenService, AuthenticationManager authenticationManager) {
		super();
		this.estabelecimentoUseCase = estabelecimentoUseCase;
		this.userUserCase = userUserCase;
		this.tokenService = tokenService;
		this.authenticationManager = authenticationManager;
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO body) {
		var authToken = new UsernamePasswordAuthenticationToken(body.email(), body.password());

		try{

			var authentication = authenticationManager.authenticate(authToken);
			CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
			UserDB user = userDetails.getUserDB();
			String token = tokenService.generateToken(user);
			return ResponseEntity.ok(new LoginResponseDTO(token));

		} catch (BadCredentialsException ex) {
			throw new LoginInvalidoException();
		}
	}

	@PostMapping("/registrar-usuario")
	public ResponseEntity<Void> register(@RequestBody RegisterRequestDTO dto) {
		userUserCase.createNewUser(dto);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@PutMapping("/alterar")
	public ResponseEntity<?> alterar(@RequestBody AlterarRegistDTO dto) {
		this.getUsername();

		userUserCase.updateUser(dto);
		return ResponseEntity.status(HttpStatus.OK).build();
	}

	@PostMapping("/registrar-estabelecimento")
	public ResponseEntity<?> registro(@RequestBody NewRegisterDTO dto) {
		estabelecimentoUseCase.createNewEstabelecimento(dto);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	private String getUsername(){
		CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return userDetails.getUsername();
	}
}