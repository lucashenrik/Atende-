package com.lucas.demo.controllers;

/*@RestController
@RequestMapping("/auth")
public class LoginControler {

	@Autowired
	private AuthService authService;

	@PostMapping("/login")
	public ResponseEntity<String> login(@RequestParam String username, @RequestParam String password,
			HttpSession session) {

		if (authService.autenticacao(username, password)) {
			session.setAttribute("user", username);

			return ResponseEntity.ok("Login sucesso!");
		}

		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	}
}*/