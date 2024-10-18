package com.lucas.demo.controllers;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotificationController {

	private final SimpMessagingTemplate template;

	public NotificationController(SimpMessagingTemplate template) {
		this.template = template;
	}

	@GetMapping("/send-notification")
	public String sendNotification() {
		String message = "Nova notificação!";
		template.convertAndSend("/topic/notifications", message);
		return "Notificação enviada!";
	}

	// Método para processar uma mensagem recebida
	@MessageMapping("/send")
	@SendTo("/topic/notifications")
	public String processMessage(String message) {
		return message; // Envia a mensagem de volta para o frontend
	}

	// Envia uma mensagem para todos os clientes conectados
	public void sendNotification(String message) {
		template.convertAndSend("/topic/notifications", message);
	}
}
