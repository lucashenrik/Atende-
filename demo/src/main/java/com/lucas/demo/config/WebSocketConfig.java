package com.lucas.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		config.enableSimpleBroker("/topic");
		config.setApplicationDestinationPrefixes("/app");
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
	    /*registry.addEndpoint("/ws-notifications")
	            .setAllowedOrigins("http://localhost:3000")
	            .withSockJS();*/
		//registry.addEndpoint("/ws-notifications")
		//.setAllowedOrigins("http://191.101.70.241:3000")
		registry.addEndpoint("/wss-notifications")
        .setAllowedOrigins("https://atende-mais.shop", "https://191.101.70.241:3000", "http://localhost:3000")
        .withSockJS()
        .setHeartbeatTime(25000); // Ajusta o tempo de timeout da conexão.
	}
}