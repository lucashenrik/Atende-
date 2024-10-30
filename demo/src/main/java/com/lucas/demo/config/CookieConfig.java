package com.lucas.demo.config;

import org.springframework.boot.web.servlet.server.CookieSameSiteSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CookieConfig {
	
	@Bean
	public CookieSameSiteSupplier sameSiteCookieConfig() {
		return CookieSameSiteSupplier.ofNone();
	}
}