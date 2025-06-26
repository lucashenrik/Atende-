package com.lucas.demo.infra.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.lucas.demo.infra.model.UserDB;
import com.lucas.demo.infra.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class SecurityFilter extends OncePerRequestFilter {
	@Autowired
	TokenService tokenService;

	@Autowired
	UserRepository userRepository;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		var token = this.recoverToken(request);
		if (token != null) {
			try {
			var email = tokenService.validateToken(token);
			UserDB user = userRepository.findByEmail(email)
					.orElseThrow(() -> new UsernameNotFoundException("User not found"));
			UserDetails userDetails = new CustomUserDetails(user);

			var authentication = new UsernamePasswordAuthenticationToken(userDetails, null, user.getAuthorities());
			SecurityContextHolder.getContext().setAuthentication(authentication);
			} catch (UsernameNotFoundException ex) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.setContentType("application/json");
				response.getWriter().write(
						"{\"erro\":\"" + ex.getMessage() + "\"}"
				);
				return; // NÃO chama o filterChain
			}
		}
		filterChain.doFilter(request, response);
	}

	private String recoverToken(HttpServletRequest request) {
		var authHeader = request.getHeader("Authorization");
		if (authHeader == null)
			return null;
		return authHeader.replace("Bearer ", "");
	}
}