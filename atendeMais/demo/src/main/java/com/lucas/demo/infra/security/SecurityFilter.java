package com.lucas.demo.infra.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.lucas.demo.infra.model.UserDB;
import com.lucas.demo.infra.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class SecurityFilter extends OncePerRequestFilter {
	private final AntPathMatcher matcher = new AntPathMatcher();

	@Autowired
	TokenService tokenService;

	@Autowired
	UserRepository userRepository;

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		// Pule o filtro para GET /api/v1/pedido/{idEstabelecimento}/pedidos-clientes
		return HttpMethod.GET.matches(request.getMethod())
				&& matcher.match("/api/v1/pedido/*/pedidos-clientes", request.getServletPath());
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request,
									HttpServletResponse response,
									FilterChain filterChain)
			throws ServletException, IOException {
		// Se shouldNotFilter retornou true, nem chega aqui
		String token = recoverToken(request);
		if (token != null) {
			try {
				var email = tokenService.validateToken(token);
				UserDB user = userRepository.findByEmail(email)
						.orElseThrow(() -> new UsernameNotFoundException("User not found"));
				var userDetails = new CustomUserDetails(user);

				var auth = new UsernamePasswordAuthenticationToken(
						userDetails, null, user.getAuthorities());
				SecurityContextHolder.getContext().setAuthentication(auth);
			} catch (UsernameNotFoundException ex) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.setContentType("application/json");
				response.getWriter().write("{\"erro\":\"" + ex.getMessage() + "\"}");
				return;
			}
		}
		filterChain.doFilter(request, response);
	}

	private String recoverToken(HttpServletRequest request) {
		var header = request.getHeader("Authorization");
		if (header == null || !header.startsWith("Bearer ")) return null;
		return header.substring(7);
	}
}