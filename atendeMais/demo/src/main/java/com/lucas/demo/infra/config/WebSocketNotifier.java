package com.lucas.demo.infra.config;

import com.lucas.demo.infra.model.NewNotificationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class WebSocketNotifier {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketNotifier(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void onNovaNotificacao(NewNotificationEvent event) {
        messagingTemplate.convertAndSend("/topic/notifications", event.getMensagem());
    }
}
