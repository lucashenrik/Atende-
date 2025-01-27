package com.lucas.demo.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.lucas.demo.infra.security.TokenServico;
import com.lucas.demo.model.CustomUserDetails;
import com.lucas.demo.model.EnumRoles;
import com.lucas.demo.model.Estabelecimento;
import com.lucas.demo.model.LoginRequestDTO;
import com.lucas.demo.model.NewRegisterDTO;
import com.lucas.demo.model.RegisterRequestDTO;
import com.lucas.demo.model.UserDB;
import com.lucas.demo.model.dto.AlterarRegisDTO;
import com.lucas.demo.model.dto.LoginResponseDTO;
import com.lucas.demo.repository.DepartRepository;
import com.lucas.demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private DepartRepository estabelRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private TokenServico tokenService;

	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private AuthorizationSecurity auth;

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequestDTO body) {
		var usernamePassword = new UsernamePasswordAuthenticationToken(body.email(), body.password());
		var auth = authenticationManager.authenticate(usernamePassword);

		// var token = tokenService.generateToken((UserDB)auth.getPrincipal());

		CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
		UserDB user = userDetails.getUserDB();

		var token = tokenService.generateToken(user);

		return ResponseEntity.ok(new LoginResponseDTO(token));
	}

	@PostMapping("/registrar-usuario")
	public ResponseEntity<?> register(@RequestBody RegisterRequestDTO dto, @RequestHeader("Authorization") String authHeader) {
		String idEstabelecimento = auth.validarToken(authHeader);

		Optional<UserDB> user = this.userRepository.findByEmail(dto.email());
		
		if (user.isEmpty()) {
			String passwordEnconder = passwordEncoder.encode(dto.password());
			
			Estabelecimento estabelecimento = estabelRepository.findByEmail(idEstabelecimento).get();
			
			UserDB newUser = new UserDB(dto.name(), dto.email(), passwordEnconder, EnumRoles.USER, estabelecimento);

			this.userRepository.save(newUser);
			return new ResponseEntity<>(HttpStatus.CREATED);
		}

		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}
	
	@PostMapping("/registrar-estabelecimento")
	public ResponseEntity<?> registro(@RequestBody NewRegisterDTO dto) {

		Optional<UserDB> user = this.userRepository.findByEmail(dto.email());
		
		if (user.isEmpty()) {
			String passwordEnconder = passwordEncoder.encode(dto.password());
			
			Estabelecimento newEstabelecimento = new Estabelecimento(dto.name(), dto.telefone(), dto.email());
			this.estabelRepository.save(newEstabelecimento);
			Estabelecimento estabelecimento = estabelRepository.findByEmail(dto.email()).get();
			
			UserDB newUser = new UserDB(dto.name(), dto.email(), passwordEnconder, EnumRoles.USER, estabelecimento);
			this.userRepository.save(newUser);
			
			return new ResponseEntity<>(HttpStatus.CREATED);
		}
		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}
	
	@PutMapping("/alterar")
	public ResponseEntity<?> alterar(@RequestBody AlterarRegisDTO dto, @RequestHeader("Authorization") String authHeader){
		String idCliente = auth.validarToken(authHeader);
		
		if (idCliente == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		
		try {
			var usernamePassword = new UsernamePasswordAuthenticationToken(dto.email(), dto.senhaAntiga());
			var auth = authenticationManager.authenticate(usernamePassword);

			CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
			UserDB user = userDetails.getUserDB();
			
			String senhaCripto = passwordEncoder.encode(dto.novaSenha());
			user.setPassword(senhaCripto);
			
			this.userRepository.save(user);
			
			return new ResponseEntity<>(HttpStatus.OK);
			
		} catch (Exception e) {
			throw e;
		}
	}
}