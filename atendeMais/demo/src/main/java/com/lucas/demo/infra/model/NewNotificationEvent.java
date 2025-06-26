package com.lucas.demo.infra.model;

public class NewNotificationEvent {

    private final String mensagem;
    public NewNotificationEvent(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getMensagem() {
        return mensagem;
    }
}
